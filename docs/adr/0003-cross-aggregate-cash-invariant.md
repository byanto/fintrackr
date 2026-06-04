# ADR-003: Cross-Aggregate Cash Invariant (BrokerAccount RDN ↔ Portfolio Trading Balances)

- **Status:** Accepted (amended 2026-05-31)
- **Date:** 2026-05-24
- **Deciders:** Budi Yanto

## Context

Each `BrokerAccount` in Fintrackr holds a single regulated cash balance (RDN — *Rekening Dana Nasabah*, governed by OJK in Indonesia). The user organizes investments within a broker account into multiple `Portfolio`s (e.g., "Long-Term," "Trading"), and each `Portfolio` carries its own *trading balance* — a logical allocation of the broker's RDN.

This produces a **cross-aggregate invariant**:

> `sum(brokerAccount.portfolios.tradingBalance) == brokerAccount.rdn`

This invariant must always hold. Operations that violate it (anywhere in the system) represent a serious data-integrity bug — money cannot be created or destroyed by accounting actions.

Classical DDD says: one aggregate = one consistency boundary. This invariant crosses two aggregates (`BrokerAccount` and `Portfolio`), so the textbook answer doesn't apply cleanly. A deliberate choice is required.

## Decision

For v1: **use a single database transaction** to update both the affected `Portfolio` (its `tradingBalance`) and the `BrokerAccount` (its `rdn`) atomically. An **application service** (in the **Portfolio Management** module — see the Amendment below) coordinates the update; it acquires both aggregates, applies the change, and commits in one transaction. Domain logic stays inside the aggregates; the application service only orchestrates.

In a modular monolith on a single database, this is straightforward and correct. If we later extract Brokerage and Portfolio Management into separate services, this ADR will be superseded by an event-driven approach (transactional outbox + listener with eventual consistency).

This orchestrated transaction is the only mechanism maintaining the invariant in v1. Domain events such as `BuyRecorded` may be emitted for read models or audit, but they do not drive the RDN update. The `TradingBalanceChanged` event referenced in early domain-model drafts is therefore unnecessary in v1 and is deferred until a consumer needs it or the modules are split.

## Amendment — 2026-05-31

The original Decision placed the coordinating application service "in the Brokerage module," justified in Open Questions by "probably Brokerage (it owns the cash invariant)." Implementation review surfaced that this conflated two distinct questions: **who owns the cash invariant** (BrokerAccount, in Brokerage) and **who owns the use case** ("record a buy," which is a Portfolio Management action).

Resolved: the orchestrating application service (`RecordBuyUseCase`, plus its Sell/Dividend/Deposit/Withdrawal siblings) lives in **Portfolio Management**. Portfolio Management is the core subdomain; Brokerage is supporting. A supporting subdomain must not depend on the core, so the dependency points **Portfolio Management → Brokerage**, exercised through Brokerage's published API (e.g., `computeBuyFee`, `applyCashFlow`) — a call through a published API is an allowed module dependency under Spring Modulith, not a boundary violation. The single-transaction mechanism and the strong-consistency guarantee of the original Decision are unchanged; only the *home* of the orchestration — and therefore the dependency direction — is fixed here.

## Alternatives Considered

- **(A) Single database transaction across both aggregates — chosen.**
  - Pros: Strongly consistent. Simple to reason about. Easy to test. Works perfectly in a modular monolith.
  - Cons: Couples the two aggregates more tightly than DDD purists prefer. Migration to services later requires reworking this part.

- **(B) Domain events with eventual consistency.**
  - Pros: Decoupled aggregates. Naturally fits a future service split.
  - Cons: Brief window of inconsistency between aggregates. Outbox or saga needed for reliability. Significant complexity for v1 needs.
  - Rejected for v1 because it solves problems we don't have yet.

- **(C) Compute RDN from the sum of trading balances — no separate `rdn` field.**
  - Pros: Inconsistency impossible by construction. Most elegant.
  - Cons: Every RDN read requires loading all portfolios. Reconciling against externally-provided broker statements becomes awkward (RDN is reported authoritatively by the broker; we'd be deriving it instead).
  - Rejected because reconciliation against broker statements is an important future feature.

## Consequences

### Positive
- Strong consistency: the invariant cannot be violated transiently.
- Simple mental model — easy to test, easy to debug.
- Application service is a small, focused class — easy to introduce a saga later if the architecture splits.

### Negative (costs we explicitly accept)
- Stronger coupling between Brokerage and Portfolio Management modules than a pure event-driven design would have. Mitigated by keeping the coupling in one place (the application service) rather than scattering it.
- Future service split will require migrating this to a saga or transactional outbox. Acknowledged debt.

### Neutral / Open Questions
- **(Resolved, 2026-05-31)** Where does the application service live? In **Portfolio Management**, not Brokerage — see Amendment. Owning the cash invariant (Brokerage) and owning the use case (Portfolio Management) are different concerns; the use-case owner hosts the orchestrator, and the core subdomain does not become a dependency of the supporting one.
- When/how do we reconcile our cached `rdn` against the authoritative broker statement? Out of scope for v1.

## References
- ADR-001 (Modular monolith over microservices)
- Vaughn Vernon, *Implementing Domain-Driven Design* — chapter on aggregate design
- Transactional outbox pattern — microservices.io
