# ADR-014: What a Read Use Case Returns

- **Status:** Accepted
- **Date:** 2026-11-07
- **Deciders:** Budi Yanto

## Context

The first two read endpoints return a Details record from the use case
(`BrokerAccountDetails`, `PortfolioDetails`), and the controller maps it to a response. Reading a
transaction is different. `Transaction` is a sealed type, so the same pattern needs a sealed
`TransactionDetails` next to the domain type and the sealed `TransactionResponse`: three parallel
hierarchies and two `switch` statements, and `Sell` has to be added in all of them.

Two reads that work differently need a rule, or the next endpoint copies whichever file was open.

## Decision

A read use case returns a Details record when the domain object has behaviour, or when the answer
needs data the domain object does not hold. It returns the domain object directly when that object
is immutable and already has the right shape.

So `GetPortfolioUseCase` returns `PortfolioDetails` and `GetTransactionUseCase` returns
`Transaction`. In both cases the controller maps to a response record, and Jackson never sees a
domain type.

## Alternatives Considered

- **Details everywhere:**
  - One pattern, nobody has to think about it
  - For transactions the Details records copy every field and protect nothing, because the response
    records already keep the API independent of the domain
  - Rejected for now. A fair choice for a team that values uniformity over the extra classes
- **Domain object everywhere:**
  - Fewest classes
  - A controller holding a `Portfolio` can call `recordBuy`. That compiles and silently does
    nothing, since the object is a detached copy (ADR-009)
  - Rejected
- **Response record as the return type of the use case:**
  - No mapping in the controller
  - If the type lives in the web adapter the core depends on the adapter. If it lives in the port,
    the JSON shape sits in the core
  - Rejected

## Consequences

### Positive
- `Sell` is added in two places. The `switch` in the controller stops compiling until it is handled
- The API stays independent of the domain, because the response records stand between them

### Negative (costs we explicitly accept)
- Two patterns exist side by side, and a reader has to know this rule
- `GetTransactionController` knows `Deposit` and `Buy`, not only ids. The dependency points inward
  and stays inside the module

### Neutral / Open Questions
- Nothing enforces the rule. ArchUnit does not check it
- If an answer later needs data outside the domain object, such as a running balance or the asset
  name from the catalog, that use case moves to Details. The change is internal, and no client
  notices

## References
- ADR-004: Transaction ledger as source of truth
- ADR-009: Pure domain model with separate JPA persistence
- ADR-013: Portfolio ledger persistence
