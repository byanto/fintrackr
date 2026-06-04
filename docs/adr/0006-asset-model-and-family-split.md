# ADR-006: Asset Model — Position vs Cash Families; `Asset` Hierarchy Scoped to Stock and Mutual Fund for v1

- **Status:** Accepted
- **Date:** 2026-05-31
- **Deciders:** Budi Yanto

## Context

The Vision (`01-fintrackr-vision.md`) lists four instrument types as in-scope: Stocks, Mutual Funds, Bonds, and Savings. The domain model draft (`03-fintrackr-domain-model.md` §6.1) sketched a single sealed hierarchy `Asset permits Stock, MutualFund, Bond, Savings`.

However, the tactical model established in earlier ADRs is **equity-shaped**. ADR-004 (transaction ledger as source of truth) and ADR-005 (Specific Identification cost basis, with `Acquisition`, `Sell` allocations, and per-acquisition dividend attribution) were all designed around the mental model *"each Buy is a deliberate lot I want to evaluate individually."* That model fits stocks natively.

Pushing all four instrument types through this one model surfaces a leaky-abstraction smell:

- **Savings** has no quantity, no cost basis, no Sell, and no realized P&L. `savings.costBasis()` or `Savings.recordSell()` would be nonsense. It is a cash balance that accrues interest.
- **Bond** is purchased in a nominal *amount* (face value), its income is a *coupon* (not a dividend), and it has a *maturity* event (a disposal at face value) that stocks don't have.

A clean answer is required *now*, because the asset taxonomy determines (a) the `Quantity` value-object design (see ADR-008), and (b) the surface of the Portfolio aggregate and Asset Catalog, both of which we are about to implement. Deciding this badly propagates into the core domain code.

## Decision

**Split everything the user "owns" into two families, and model only one of them under `Asset`.**

1. **Position-like family** — held in a *quantity*, has a *cost basis*, receives *income*, and *realizes a gain or loss on disposal*. Members: **Stock, MutualFund, Bond**. These are the instruments the ADR-004 / ADR-005 machinery serves.
2. **Cash-like family** — a *balance* moved by deposits, withdrawals, and accrued interest; no quantity, no cost basis, no disposal, no P&L. Members: **RDN, Trading Balance, Savings**.

Concrete decisions:

- The sealed `Asset` hierarchy models **only position-like instruments**. For v1:

  ```java
  sealed interface Asset permits Stock, MutualFund {
      Symbol symbol();
      String name();
  }
  ```

- **v1 `Asset` subtype attributes.** `Stock(symbol, name, sector, currentPrice, priceAsOf)`; `MutualFund(symbol, name, currentNAV, navAsOf)`. `currentPrice` / `currentNAV` are **entered manually in v1** (there is no market-data feed yet), and each carries an **as-of timestamp** (`priceAsOf` / `navAsOf`) set whenever a price is entered. v2 automatic pulling simply repopulates the same fields. The timestamp ships in v1 because a holding's market value is only meaningful relative to the date its price is "as of" — a manual price goes stale exactly as a pulled one does.

- **Bond** is a *known, deferred, additive* third position variant. It fits the existing Acquisition/Sell/cost-basis model with two additive extensions: a coupon is an **income event** (sibling to `Dividend`), and maturity is a **disposal at face value**; its "quantity" is *units of face value*. Adding it later is additive — the exhaustive `switch` over the sealed hierarchy (and over income/transaction types) will force the compiler to flag every site that must handle it.

- **Savings is removed from `Asset`** and reclassified into the cash family as its **own entity** (`SavingsAccount`): it has independent identity, its own lifecycle (opened/closed independently), and metadata (bank name, account number, interest rate), plus an append-only interest-accrual + cash-flow ledger (consistent with ADR-004). **Savings is out of scope for v1** — deferred to a later version; the slot (a cash-family entity, as above) is recorded so it has a known home when built.

- **The cash family gets no shared (sealed) supertype in v1.** RDN, Trading Balance, and `SavingsAccount` share only *"they hold a `Money` balance"* — which is **composition**, not a behavioural hierarchy. There is no consumer in v1 that receives "a cash account" and dispatches polymorphically over which kind it is. A thin shared interface is introduced *only later, if a concrete need appears* (e.g., a net-worth read-model that must enumerate and total all cash sources).

- **RDN remains a `Money` field on `BrokerAccount`** — it is **not** promoted to a separate entity, despite having real-world bank/account-number attributes. The decision is made on **identity and lifecycle**, not attribute count: RDN is strictly one-to-one and lifecycle-bound to its `BrokerAccount` and is never referenced independently, so its identity *is* the `BrokerAccount`'s. RDN metadata (bank name, account number) is deferred until broker-statement reconciliation is built — which is already out of v1 scope per ADR-003.

## Alternatives Considered

- **(A) One sealed `Asset` hierarchy over all four types (Stock, MutualFund, Bond, Savings).**
  - Pros: superficially uniform; one abstraction for "things I own".
  - Cons: leaky abstraction. Savings exhibits none of the position behaviours; methods like `recordSell` / `costBasis` become nonsensical for it (a Liskov violation). The shared interface would have to be so thin it carries no useful common behaviour.
  - **Rejected** — the resemblance between the four types is superficial; unifying them hides genuine differences.

- **(B) Implement all four instrument types in v1.**
  - Pros: matches the Vision's stated scope verbatim.
  - Cons: Bond and Savings need genuinely different machinery (coupon vs dividend, maturity, interest accrual). Building all four trades depth for breadth within the available time.
  - **Rejected** — defer Bond and Savings with *known slots*; ship two coherent, well-tested position types.

- **(C) Model the cash family under a shared sealed `CashAccount` interface (RDN, Trading Balance, Savings).**
  - Pros: symmetry with `Asset`/`Transaction`; a single "cash" abstraction.
  - Cons: no polymorphic consumer and no behavioural difference among them in v1 — the only thing they share is holding a `Money`, which composition already gives. Their invariants differ (RDN is tied to the portfolio-sum invariant; Savings is standalone). Premature abstraction.
  - **Rejected for v1** — composition over a shared `Money` suffices. Revisit only if a net-worth read-model needs to enumerate cash sources.

- **(D) Promote RDN to its own entity (mirroring `SavingsAccount`).**
  - Pros: surface symmetry — both "look like" accounts with bank/account-number attributes.
  - Cons: entity-hood is decided by identity and lifecycle, not by attribute count. RDN has neither independent identity nor independent lifecycle (1:1, lifecycle-bound to `BrokerAccount`).
  - **Rejected** — RDN is state on `BrokerAccount`.

- **(E) Make Savings a degenerate position with a single synthetic "unit".**
  - Pros: lets it reuse the position model.
  - Cons: distorts the domain — interest is not a dividend, there are no acquisitions or sells, there is no cost basis. The model would lie about what a savings account is.
  - **Rejected.**

## Consequences

### Positive
- Models the domain as it actually is; dissimilar concepts are not forced into one abstraction (DDD-correct).
- The v1 surface is small and coherent: two quantity-bearing position types sharing one Acquisition/Sell/cost-basis model.
- Deferral is *safe* because each deferred concept has a known slot: Bond is additive over the sealed hierarchy; Savings is a cash entity parallel to RDN.

### Negative (costs we explicitly accept)
- The Vision lists four in-scope types; v1 ships two. The Vision's scope wording must be updated to reflect the deferral of Bond and Savings.
- Bond and Savings remain unimplemented; their detailed models are not yet validated against real broker/bank data — modelling risk surfaces only when they are built.
- Two cash representations coexist (a `Money` field on an entity for RDN/Trading Balance, vs a `SavingsAccount` entity). A future net-worth view must reconcile across both shapes.

### Neutral / Open Questions
- **(Resolved) Savings:** out of scope for v1 — deferred. Its owning context is decided when it is built (clearly *not* Brokerage — likely a lightweight "Banking"/"Cash" context, or absorbed into a future net-worth read-model).
- **(Resolved) Market price / NAV:** entered **manually in v1**; automatic pulling deferred to v2. `MutualFund`'s identifier is modelled as a `Symbol` so the `Asset` contract (`symbol()`, `name()`) holds uniformly across subtypes.
- **(Resolved) As-of timestamp:** `priceAsOf` / `navAsOf` ship in **v1**, set on manual entry and later repopulated by v2 auto-pull — because market value is only meaningful relative to the price's as-of date.
- **When (if ever) a shared `CashAccount` interface becomes justified** — only on a real polymorphic need (e.g., a net-worth read-model enumerating cash sources).

## References
- ADR-001 — Modular monolith with Spring Modulith
- ADR-003 — Cross-aggregate cash invariant (RDN / trading balances; reconciliation deferred)
- ADR-004 — Transaction ledger as source of truth; Holding cached
- ADR-005 — Specific Identification cost basis with per-acquisition dividend attribution
- ADR-008 — Quantity and Percentage value-object scale & rounding policies (the Quantity design that follows from this taxonomy)
- Fintrackr Vision (`01-fintrackr-vision.md`)
- Fintrackr Domain Model (`03-fintrackr-domain-model.md`)
- Eric Evans, *Domain-Driven Design* (entities vs value objects; modelling the domain as the expert sees it)
- Vaughn Vernon, *Implementing Domain-Driven Design* (aggregate and boundary design)
