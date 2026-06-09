# ADR-007: Currency Scale and Rounding Policy for IDR

- **Status:** Accepted (amended 2026-06-08)
- **Date:** 2026-05-25
- **Deciders:** Budi Yanto

## Context

Before implementing core domain logic that alters financial state (such as `recordBuy` or `recordSell`), we must establish a rigorous, application-wide standard for how monetary values are computed, rounded, and stored.

While the use of `BigDecimal` over floating-point primitives is a given for financial software, `BigDecimal` alone does not prevent precision errors; it merely forces the developer to make explicit choices about scale and rounding. Because Fintrackr primarily deals with Indonesian Rupiah (IDR), which practically lacks fractional subunits, we must decide how to handle fractional results that emerge from intermediate calculations (e.g., fractional share allocations, percentage-based fee calculations, or tax math) before those values cross the boundary into persistent state. A decision is required now to ensure the integrity of the cross-aggregate cash invariants.

## Decision

Fintrackr will use `RoundingMode.HALF_EVEN` (Banker's Rounding) with a strict scale of 0 for all instantiated `Money` objects.

Concretely:
- Data Structure: `Money` is implemented as a Java 21 `record`.
- Normalization Point: Rounding is invoked exclusively within the compact constructor of the Money record. It applies `.setScale(0, RoundingMode.HALF_EVEN)` to every input.
- Public API: Instantiation is exposed strictly via factory methods (e.g., `Money.of()`, `Money.zero()`). These route through the compact constructor, making it an unbypassable normalization checkpoint.
- Intermediate Computation: Domain services and aggregates may use raw `BigDecimal` values of any scale during intermediate steps, but they must be normalized into a `Money` record before being applied to aggregate state.

Worked Example:
Computing a buy fee for `quantity = 100`, `price = 9850`, `feeRate = 0.0015`:

```java
rawFee = 100 * 9850 * 0.0015 = BigDecimal("1477.5000")    // scale 4 (Illustrative)
Money fee = Money.of(rawFee)                              // routes through compact constructor

// Compact constructor: 1477.5 -> 1478 (HALF_EVEN picks the even neighbor)
// fee.amount() == BigDecimal("1478"), scale 0
```
*Compare: had the price been 9851, rawFee = 1477.65 -> rounds to 1478 (HALF_EVEN, no exact-half case). Had it been an exact-half case like 1478.5, HALF_EVEN picks 1478; HALF_UP would have picked 1479.*

## Amendment — 2026-06-08: JavaMoney/JSR-354 considered and rejected; record confirmed

Implementing `Money` prompted an evaluation of JavaMoney (JSR-354, reference impl Moneta) and Joda-Money as alternatives to the hand-rolled `BigDecimal`-backed value object.

- **JavaMoney / JSR-354 — rejected for v1.** It is a finalised JSR but not part of the JDK (an external dependency). Its strengths — multi-currency, FX, locale formatting, pluggable rounding — are exactly what v1 scopes out. Adopting it now would: import a dependency into the domain layer (against ADR-009's dependency-light domain); complicate persistence (mapping `MonetaryAmount` through JPA/MapStruct versus a single `NUMERIC` column); and weaken the construction-time normalisation guarantee (JavaMoney rounds via an explicit operation, not at construction), forcing a wrapper that re-creates this VO. Crucially, the cheap multi-currency *transition* the library appears to offer is already provided by **encapsulation** — `Money` already carries a `Currency` and is built only via factory/compact constructor, so the implementation is a swappable detail behind that seam. Multi-currency's hard parts (FX policy, the cross-currency cash invariant, consolidation currency) are domain changes the library does not solve.
- **Joda-Money — also rejected for v1.** Lighter than JavaMoney (money without the FX framework), but still more than a single-currency, scale-0 IDR value object needs.
- **Decision unchanged:** keep the hand-rolled `record Money`. If multi-currency lands (the deferred v2 item), revisit Joda-Money/JavaMoney *then*, swapping the implementation behind the existing factory seam.

**Record vs class (settled):** keep `Money` a `record`. A public record's canonical constructor cannot be made more restrictive than the record, so it cannot be hidden — but because validation/normalisation lives in the compact constructor, *every* construction path is already safe. Switching to a class with a private constructor plus factory would trade away records' correct `equals`/`hashCode`, immutability, and pattern-matching for no safety gain. Reach for class-plus-private-constructor only when the factory must do more than construct (caching, polymorphic returns) — which `Money` does not.

## Alternatives Considered

For each alternative:

- **(A) `RoundingMode.HALF_UP` with Scale 0**
    - Pros: Matches "grade school" math expectations; conceptually simple.
    - Cons: None in the context of this specific system's low transaction volume.
    - Why rejected: While the statistical bias of `HALF_UP` is negligible at the transaction volume of a personal portfolio tracker, `HALF_EVEN` is the universally expected default in the financial industry. We choose `HALF_EVEN` not because the statistical balancing is mathematically necessary for our scale, but because industry convention beats personal preference at equal cost.

- **(B) `RoundingMode.HALF_DOWN`**
    - Pros: None in this context.
    - Cons: Asymmetric and obviously mathematically flawed for standard rounding contexts. It introduces a downward bias that is counterintuitive to both standard arithmetic and financial conventions.
    - Why rejected: Fundamentally incorrect for general currency rounding.

- **(C) Retain decimals (Scale 2 or higher) for IDR storage**
    - Pros: Retains absolute precision of intermediate calculations in the database.
    - Cons: Unnecessary complexity. IDR (sen) subunits have been completely out of circulation for decades. Storing fractions of IDR creates UI clutter and database bloat without adding business value.
    - Why rejected: IDR has no functional subunit in practice.

- **(D) MathContext (Precision-based rounding)**
    - Pros: Mathematically rigorous for maintaining significant digits across complex scientific calculations.
    - Cons: Conceptual mismatch. Financial ledgers are aligned by fixed decimal places (scale), not by total significant digits (precision).
    - Why rejected: Scale-based rounding (setScale) is the domain standard for currency over precision-based rounding.

## Consequences

### Positive
- Guaranteed Consistency: By enforcing the scale and rounding mode in the `Money` compact constructor, it is impossible for the rest of the application to accidentally persist or calculate with fractional IDR.
- Single Point of Normalization: every `Money` is guaranteed scale-0 because every construction path runs through one compact constructor. 
- Familiarity: Aligns with standard Java/financial engineering practices, satisfying reviewer and auditor expectations.

### Negative (costs we explicitly accept)
- Silent rounding hides bugs: The compact constructor applies silent rounding and lacks magnitude or precision validation. A bug that produces an absurd intermediate value (e.g., from a wrong unit assumption) will be silently normalized to scale-0 rather than rejected.
    - Mitigation: This is mitigated by invariant checks within domain methods (e.g., `Portfolio.recordBuy` validates that a fee is non-negative and reasonable relative to the trade size), but the `Money` record itself does not catch this.
- Multi-Lot Allocation Drift: Because all Money objects strictly round to 0 decimals, distributing a total fee across multiple transaction lots (e.g., partial fills) can produce a ±1 IDR drift between `sum(allocations)` and `totalFee`.
    - Mitigation: This is a known consequence of integer-based allocation. We accept this debt for the backlog and will handle it using a "remainder-to-last-allocation" algorithm when implementing `recordSell` and multi-lot fills.

### Neutral / Open Questions
- Negative Money: How do we represent negative balances versus negative deltas? This will be addressed by invariants within the `Money` record itself, independent of the rounding policy.
- Multi-Currency: Out of scope for Fintrackr v1. If USD or EUR support is added later, the strict 0 scale policy will need to be refactored to support variable scales per currency.
- Database column type for persisted Money values: Likely `NUMERIC(19, 0)` to preserve the `BigDecimal` semantics, but `BIGINT` is also viable given scale-0 storage. Defer to when persistence is implemented.
- Scope: This policy governs `Money` only. `Quantity` (which may be fractional for mutual fund units) and `Percentage` (fee rates, scale ~4) have separate scale policies, deferred to a future ADR or decided when those value objects are implemented.

## References
- ADR-003: Cross-Aggregate Cash Invariant (rounding directly impacts our ability to mathematically guarantee this invariant).
- ADR-004: Transaction Ledger as Source of Truth (ensuring rounded values in the ledger do not cause drift in the derived cache).
- ADR-005: Specific Identification Cost Basis (the source of multi-lot sells which trigger the multi-lot allocation drift).