# ADR-005: Specific Identification Cost Basis with Per-Acquisition Dividend Attribution

- **Status:** Accepted
- **Date:** 2026-05-24
- **Deciders:** Budi Yanto

## Context

Calculating cost basis, realized P&L on sells, and dividend attribution depends on the chosen cost-basis method. The method has a deep impact on the domain model — much deeper than a typical "configuration" choice — because Specific Identification requires acquisition-level tracking while Weighted Average needs only aggregate values per holding.

Reviewing the user's existing Google Sheets workflow surfaced concrete requirements that constrain the choice:

1. **Each Buy is a deliberate investment decision.** The user records the date, price, fee, and quantity of every buy and treats each as a distinct *acquisition*.
2. **Per-acquisition performance evaluation is wanted.** The user needs to assess each individual decision, not just the portfolio-wide average.
3. **Per-acquisition dividend attribution is wanted.** Dividends received while an acquisition was held should be allocated back to that acquisition, enabling true per-acquisition total return (price gain + dividends) and per-acquisition CAGR.
4. **Sells consume specific acquisitions.** Either by explicit user choice each time, or by an explicit strategy (oldest first, highest cost first, etc.), with the option to override.
5. **Multi-acquisition sells happen.** A single sell transaction may consume shares from multiple acquisitions.
6. **Buy history must be immutable.** Once recorded, the original Buy is a historical fact and should never be destructively edited; corrections happen via reversing transactions.

These requirements rule out Weighted Average and force a acquisition-aware model.

## Decision

Adopt **Specific Identification cost basis with per-acquisition dividend attribution**, implemented via an immutable `Acquisition` entity referenced by `Sell` allocations.

### Acquisition model

`Acquisition` is a first-class entity in Portfolio Management:

```
Acquisition (entity, immutable on creation)
├─ id
├─ portfolioId
├─ assetId
├─ openDate
├─ openPrice
├─ openFee
├─ initialQuantity                      ← original, never changes
├─ remainingQuantity (derived)          ← initialQuantity − Σ sell allocations against this Acquisition
├─ status (derived)                     ← OPEN | PARTIALLY_CLOSED | CLOSED
└─ dividendAllocations: List<DividendAllocation>
```

Each Buy creates exactly one Acquisition. An Acquisition is never mutated after creation; its lifecycle progresses through Sell allocations and Dividend allocations that reference it.

### Sell model

A `Sell` is one transaction at one market price, possibly consuming from multiple Acquisitions:

```
Sell (entity)
├─ id
├─ portfolioId
├─ assetId
├─ date
├─ price
├─ totalQuantity
├─ totalFee
└─ allocations: List<SellAllocation>

SellAllocation (value object)
├─ acquisitionId  ─────────► references a Acquisition
├─ sharesSoldFromAcquisition
└─ feeAllocated     ← proportional slice of Sell.totalFee
```

Invariants:
- The sum of `sharesSoldFromAcquisition` across allocations equals the Sell's total quantity.
- Each `SellAllocation.sharesSoldFromAcquisition ≤ targetAcquisition.remainingQuantity` at the time the Sell is recorded.
- Fee is allocated proportionally to shares. Algebraically: `feeAllocated = totalFee × (sharesSoldFromAcquisition / totalQuantity)`, which equals `sharesSoldFromAcquisition × price × sellRate` — so proportional is mathematically consistent with the underlying broker fee formula.

### Dividend attribution

For each dividend declaration (cum-date, payment-date, dividend-per-share):

```
For each Acquisition of the dividend's symbol in the portfolio:
    sharesEligibleAtCumDate
        = acquisition.initialQuantity
          − Σ SellAllocations from this acquisition WHERE sell.date ≤ cumDate

    if (acquisition.openDate ≤ cumDate AND sharesEligibleAtCumDate > 0):
        allocation = sharesEligibleAtCumDate × dps
    else:
        allocation = 0
```

A `DividendAllocation` value object is recorded against each eligible Acquisition, storing the cum-date, payment-date, DPS, and computed amount.

### Acquisition selection strategy

When a Sell is recorded, the list of `SellAllocation`s can be supplied explicitly (manual) or resolved by a strategy. Modelled as a Java 21 sealed interface:

```java
sealed interface AcquisitionSelectionStrategy
    permits Fifo, Lifo, HighestCost, LowestCost, Manual { }

public record Fifo()                                  implements AcquisitionSelectionStrategy {}
public record Lifo()                                  implements AcquisitionSelectionStrategy {}
public record HighestCost()                           implements AcquisitionSelectionStrategy {}
public record LowestCost()                            implements AcquisitionSelectionStrategy {}
public record Manual(List<SellAllocation> allocations) implements AcquisitionSelectionStrategy {}
```

Each `Portfolio` has a `defaultAcquisitionSelectionStrategy`. Every Sell may override it (most commonly by supplying `Manual` with explicit allocations). Exhaustive `switch` pattern matching in the Portfolio aggregate ensures every strategy variant is handled, with compile-time errors if a new variant is added without a handler.

### Where these methods live

All of this is exposed only through the `Portfolio` aggregate (per ADR-004 — only the aggregate may mutate Acquisitions and Holdings):

- `portfolio.recordBuy(symbol, quantity, price, fee, date)` → opens a new Acquisition
- `portfolio.recordSell(symbol, quantity, price, fee, date, strategy)` → records a Sell with resolved allocations
- `portfolio.recordDividend(symbol, dps, cumDate, paymentDate)` → allocates to eligible Acquisitions

## Alternatives Considered

- **Weighted Average** (initial draft of this ADR — rejected).
  - Pros: simplest model; smallest persistence footprint; no acquisition tracking required.
  - Cons: smears individual buy decisions; cannot attribute dividends per-acquisition; per-acquisition CAGR not computable; does not match the user's actual workflow.
  - **Why this matters:** the initial recommendation was made without reviewing the user's existing tracking workflow. Revising in light of new domain context is the correct engineering response.

- **FIFO (First-In, First-Out).**
  - Pros: standard in some tax regimes; preserves per-acquisition data.
  - Cons: forces a fixed consumption order; not how the user actually decides; loses flexibility.
  - Rejected.

- **LIFO (Last-In, First-Out).**
  - Pros: occasionally tax-advantageous in some jurisdictions.
  - Cons: same fixed-order problem; not standard in Indonesia; user does not need it.
  - Rejected.

- **Specific Identification implemented by *splitting* acquisitions on partial sell** (instead of reference-based consumption).
  - Pros: each acquisition has a single lifecycle (open → closed).
  - Cons: fragments original Buy history into multiple post-sell acquisitions; the immutable historical fact "I bought 2,000 shares on this date" is lost; partial sells produce sprawl in the data.
  - Rejected in favor of the reference-based model where Sells reference Acquisitions via allocations and the Acquisition itself never changes.

## Consequences

### Positive

- Matches the user's mental model exactly (DDD-correct: model the domain as the domain expert sees it).
- Each Buy is preserved as an immutable historical fact.
- Per-acquisition P&L including dividend income is correctly attributable.
- Per-acquisition CAGR is meaningful.
- Reference-based consumption is a clean, event-sourcing-flavored pattern: Acquisition creation is one event; Sell allocations are events that progress the Acquisition's state; no destructive edits.
- Sealed `AcquisitionSelectionStrategy` produces type-safe, exhaustive handling — adding a new strategy in the future will force the compiler to flag every place that needs updating.

### Negative (costs we explicitly accept)

- Significantly richer model than Weighted Average — more entities and value objects, more invariants to enforce, more tests to write.
- Acquisitions accumulate over time. For a long-running portfolio with thousands of acquisitions per symbol, queries may need indexed access on `(portfolioId, symbol, status)`. Performance optimisation deferred until needed.
- Multi-acquisition sell logic is non-trivial; strategy resolution must be tested carefully.
- Migration risk: changing cost-basis methods later (e.g., to comply with a tax rule) would require recomputation — but this is true of any cost-basis decision and not specific to Specific Identification.

### Neutral / Open Questions

- Should fully closed Acquisitions stay accessible in the `Portfolio` aggregate, or be archived in a separate "closed acquisitions" read model? Functionally, the transaction ledger always holds the history (per ADR-004); the question is one of aggregate footprint. Defer until the in-memory aggregate size becomes a concern.

## References

- ADR-001 (Modular monolith)
- ADR-004 (Transaction ledger as source of truth)
- Fintrackr Vision (single-currency IDR, personal-use scope)
- *Domain-Driven Design* — Eric Evans (aggregate design)
- Greg Young on event sourcing (for the conceptual lineage of reference-based, append-only mutation)
