# ADR-004: Transaction Ledger as Source of Truth; Holding State as Derived Cache

- **Status:** Accepted
- **Date:** 2026-05-24
- **Deciders:** Budi Yanto

## Context

A `Holding` in Fintrackr represents the current snapshot of an asset position in a portfolio — quantity owned, average cost per share, total invested value, etc. These values are not *primary* data; they are derived from the sequence of `Transaction`s (buys, sells, dividends, deposits, withdrawals) that produced them.

A foundational architectural question must be answered: **what is the source of truth?**

- (A) **Transactions are truth.** Holding state is derived from them on every read (event-sourcing style).
- (B) **Holding state is truth.** Transactions are commands that mutate holding state, then are not strictly needed afterwards.
- (C) **Hybrid.** Transactions are the immutable ledger (truth). Holdings cache the rollup for read efficiency.

This choice has consequences for auditability, performance, correctness, and recoverability from bugs.

## Decision

Adopt **option (C): Transactions are the immutable source of truth; Holding state is a cached rollup**.

Concretely:
- Transactions are append-only. Once recorded, a Transaction is never modified or deleted. Corrections are made by recording a *reversing* transaction.
- Holdings store derived values (`quantity`, `averageCost`, `investedValue`) for fast reads.
- The **only way** to mutate a Holding is by recording a Transaction through the Portfolio aggregate root. The aggregate updates the cached Holding state atomically inside the same operation. Direct setters on Holding are not exposed (Tell, Don't Ask).
- A rebuild path exists: if a bug ever causes Holding state to drift from the transaction log, we can recompute Holdings deterministically from the ledger. This is a safety net, not a routine operation.

## Alternatives Considered

- **(A) Pure event-sourcing — derive Holdings on every read.**
  - Pros: Single source of truth. Time travel (state at any past point). No drift possible.
  - Cons: Significant infrastructure overhead (event store, snapshots, projections). Higher cognitive cost for a portfolio of this scale. Likely overkill for a personal-use app in v1.
  - Rejected as premature complexity.

- **(B) State-as-truth — discard or de-emphasize the transaction log.**
  - Pros: Simplest model. Fastest reads. No sync concerns.
  - Cons: No audit trail. Cannot recompute history. Bugs corrupt state permanently. Unacceptable for anything dealing with money.
  - Rejected on correctness/auditability grounds.

- **(C) Hybrid: ledger of truth + cached holding state — chosen.**
  - Pros: Audit trail preserved. Fast reads. Recoverable from bugs (rebuild from ledger). Reasonable cognitive load.
  - Cons: Two representations of the same information must stay in sync. Mitigated by routing all mutation through the aggregate.

## Consequences

### Positive
- Auditability: complete history of every change is preserved.
- Performance: holding-state reads are O(1), not O(transaction-count).
- Recovery: any drift between cached state and ledger can be detected and rebuilt.
- Forces clean design: mutation must go through aggregate methods (`recordBuy`, `recordSell`, …), enforcing invariants from the inside.

### Negative (costs we explicitly accept)
- Two representations of the same data — sync risk if discipline slips. Mitigated by hiding Holding mutators behind the aggregate.
- Sealed-data discipline: refactoring later that bypasses the aggregate would silently corrupt invariants. Tests around the aggregate must catch this.

### Neutral / Open Questions
- Implementation of the rebuild path: a CLI command? A scheduled health check that diffs cached vs computed state and alerts? Defer to a later ADR.
- Could we eventually move to full event-sourcing if the audit needs grow? Yes — Option C is a stepping stone toward (A) if ever needed.

## References
- ADR-001 (Modular monolith)
- *Domain-Driven Design* — Eric Evans (chapter on aggregates and repositories)
- Greg Young's writings on event sourcing (for context on what we *aren't* doing yet)
