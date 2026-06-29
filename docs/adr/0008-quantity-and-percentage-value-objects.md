# ADR-008: Scale and Rounding Policies for `Quantity` and `Percentage`

- **Status:** Accepted
- **Date:** 2026-05-31
- **Deciders:** Budi Yanto

## Context

ADR-007 fixed the scale and rounding policy for **`Money`** (`HALF_EVEN`, scale 0, normalization in the compact constructor, factory methods as the public API) and explicitly scoped itself to currency — deferring `Quantity` and `Percentage`.

But a fee calculation, `quantity × price × rate`, involves all three value objects, and `Quantity`/`Percentage` carry their *own* scale questions with *different* answers from `Money`. These must be settled before the core domain methods (`recordBuy`, `recordSell`) are written, since they decide what those methods validate and how arithmetic behaves.

Two facts from ADR-006 constrain the choice:

1. Only **Stock** and **MutualFund** are quantity-bearing in v1. Stock quantities are **whole**; mutual-fund **unit counts are fractional**.
2. Mutual-fund unit counts are **entered as reported by the broker** (a recorded fact per ADR-004), not computed by us from amount ÷ NAV.

## Decision

### `Quantity`

- **A single `Quantity(BigDecimal value)` value object.** Asset-type-specific validation is supplied by **factory methods**, *not* by separate types — because share and unit quantities behave **identically** after construction (sum, subtract, compare); they differ *only* in construction-time validation. (The test for splitting into separate types is a *behavioural* difference, which is absent here.)
  - `Quantity.ofShares(BigDecimal)` — scale 0; **rejects** any fractional value. A fractional share is a bug, not a rounding situation.
  - `Quantity.ofUnits(BigDecimal)` — fractional; **canonical scale 4** (matches Indonesian *reksa dana* unit reporting); **rejects** input finer than scale 4 rather than rounding it.
- **`Quantity` never rounds.** It stores the value faithfully and rejects malformed input. Rationale: a quantity — especially an MF unit count — is a *recorded fact* from the broker (ADR-004); silently rounding it would desync our ledger from the broker. This is the deliberate **opposite** of `Money`, which *rounds* because IDR has no functional subunit. (Two value objects, two rounding stances, each justified by domain meaning.)
- **Scale is canonicalized at construction** (pad to the canonical scale; reject if finer). Because every share quantity is then scale 0 and every unit quantity is the same fixed scale, two numerically-equal quantities always carry the same scale — which makes the record's generated `equals`/`hashCode` correct *despite* `BigDecimal.equals` considering scale (`5.0` ≠ `5.00`). No custom `equals` needed.
- **Intrinsic guards**: `≥ 0` for derived state enforced in the VO (structural guard), `value > 0` for transaction quantities enforced in the aggregate when recording a transaction (business rule).

### `Percentage`

- **`Percentage(BigDecimal rate)` storing the decimal rate** (e.g., `0.0015` for 0.15%), not a percent figure (`0.15`).
- **Validation:** `0 ≤ rate ≤ 1`. Absurd-but-valid values (e.g., `1.0` = 100%) are caught by domain *reasonableness* checks at the aggregate, not by the value object — consistent with ADR-007's separation of VO-level validation from aggregate-level reasonableness.
- **Scale: 6.** Real IDX brokerage rates require it — e.g., `0.1403%` = `0.001403`, which is exactly scale 6.
- **Round using HALF_EVEN.** `Percentage` is *configuration / input* that feeds `quantity × price × rate`; the **result** is normalized to `Money` (scale 0) at the `Money` boundary per ADR-007. The rate is never itself persisted as authoritative money, so its precision is low-stakes: being generous (scale 6) costs nothing, whereas too *few* decimals would truncate a real rate. **The danger to guard against is too few decimals, not too many.**

### Supporting policy (reaffirmed, not new)

The **fee amount recorded on a transaction is the source of truth** (expected-vs-actual; the actual recorded fee always wins). `FeeStructure`'s rate is a **pre-fill + reasonableness-check helper**, not a determinant of stored truth — which is *why* the rate's precision is non-critical. This restates domain-model §5.4 ("compute by default, allow override") and is consistent with Session 2's decision that the fee is computed in Brokerage / an application service and `recordBuy` receives a finished `Money`.

## Alternatives Considered

### `Quantity`
- **Two types, `ShareQuantity` and `UnitQuantity`** ("make illegal states unrepresentable").
  - Pros: the wholeness rule is baked into the type; a fractional share is unconstructable; maximal type-safety.
  - Cons: forces generics (`Acquisition<Q extends Quantity>`) through the *shared* Acquisition/Sell/Holding model, or a sealed `Quantity` whose arithmetic must guard same-kind operations; adds persistence complexity. All for a difference that is purely construction-time validation, not behaviour.
  - **Rejected** — two types don't earn their cost when behaviour is identical.
- **A single `Quantity` that rounds (like `Money`) at a fixed scale.**
  - Cons: rounding a broker-reported unit count desyncs our ledger from the broker; quantities should *reject* malformed input, not silently round it.
  - **Rejected.**
- **Compute MF units as amount ÷ NAV instead of recording the broker's figure.**
  - Cons: introduces our own division/rounding and drift; contradicts ADR-004 (record facts, don't derive truth).
  - **Rejected.**

### `Percentage`
- **Store as a percent (`0.15`) rather than a rate (`0.0015`).**
  - Cons: scatters `÷ 100` across every calculation; error-prone.
  - **Rejected** — the rate form is the calculation-ready standard.
- **Scale 2 or 4.**
  - Cons: truncates legitimate rates (`0.1403%` needs scale 6).
  - **Rejected.**

## Consequences

### Positive
- Three numeric value objects — `Money`, `Quantity`, `Percentage` — with deliberately *different* scale/rounding stances, each justified by domain meaning. A strong correctness story.
- A single `Quantity` type keeps the shared Acquisition/Sell model simple (no generics) while still enforcing wholeness for shares at construction time.
- Construction-time scale canonicalization makes value equality safe across all three value objects (the same constructor-as-normalization-checkpoint pattern as ADR-007).
- A generous `Percentage` scale removes a fragile precision dependency from fee math.

### Negative (costs we explicitly accept)
- With one `Quantity` type, a share quantity and a unit quantity are the *same* Java type; nothing at the type level prevents mixing them in arithmetic. Mitigated because, within a single asset's holding, all quantities are the same kind, so mixing never arises in correct code. A runtime same-kind guard can be added if a need appears. We accept slightly weaker type-safety in exchange for a simpler shared model.
- Unit scale is fixed at 4. If a broker ever reports *reksa dana* units at finer than 4 dp, that input is **rejected by design** (not silently rounded) and the policy is revisited — a deliberate trade of flexibility for ledger-to-broker fidelity.

### Neutral / Open Questions
- **(Resolved) MutualFund unit scale:** fixed at 4 dp.
- **`Quantity` arithmetic surface** (`plus` / `minus` / `compareTo`) and whether a runtime same-kind guard is warranted — decide when implementing.
- **Bond quantity** ("units of face value") — its scale policy is decided when Bond is implemented (deferred per ADR-006).

## References
- ADR-003 — Cross-aggregate cash invariant
- ADR-004 — Transaction ledger as source of truth (recorded facts, not derived values)
- ADR-005 — Specific Identification cost basis (multi-lot sells, fee allocation)
- ADR-006 — Asset model & family split (which instruments are quantity-bearing in v1)
- ADR-007 — Currency scale and rounding policy for `Money` (the sibling policy this one extends)
- Fintrackr Domain Model (`03-fintrackr-domain-model.md`) — value objects in §4.3
