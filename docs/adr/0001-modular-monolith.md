# ADR-001: Modular Monolith (Spring Modulith) over Microservices for Fintrackr

- **Status:** Accepted
- **Date:** 2026-05-23
- **Deciders:** Budi Yanto

## Context

Fintrackr is a personal investment-tracking application developed and maintained by a single developer (me), running locally at first and eventually deployed to a small cloud environment. Initial traffic is one user (me); future growth is uncertain but expected to remain small.

There is a temptation in modern backend development to default to microservices — partly because they are fashionable. A counter-trend in industry is the modular monolith: operational simplicity of a single deployable, with strict internal module boundaries enforced by tooling.

The architectural style chosen for v1 must:

1. Match the actual scale and team size (1 developer, ~1 user).
2. Demonstrate sound architectural reasoning and explicit tradeoff analysis.
3. Allow me to learn DDD, Spring, and bounded-context thinking effectively.
4. Leave a credible migration path if future scale or team requires service extraction.

## Decision

Build Fintrackr as a **modular monolith** using **Spring Modulith** to enforce module boundaries at build time. Apply **hexagonal architecture** within each module and **DDD tactical patterns** (aggregates, value objects, domain events) inside the domain layer.

## Alternatives Considered

- **Microservices from the start.** Rejected. Microservices solve an organizational problem (independent deploys for multiple teams) that does not exist here. They would impose costs — distributed transactions, network partitioning, deployment complexity, eventual consistency, service discovery — for zero benefit at this scale. This would be premature distribution.

- **Classic layered monolith** (Controller → Service → Repository, no enforced module boundaries). Rejected. Easy to start with, but degenerates into a "big ball of mud" without enforced boundaries. Misses the opportunity to learn and demonstrate bounded-context thinking. Not differentiated for portfolio purposes.

- **Modular monolith without Spring Modulith** (manual module discipline). Rejected. Without tooling, illegal cross-module dependencies sneak in over time. Spring Modulith verifies module boundaries at build time and adds typed events between modules and auto-generated documentation for low extra cost.

## Consequences

### Positive
- Operational simplicity: single deployable, single database (initially), single JVM, easy local development.
- Module boundaries enforced at build time via Spring Modulith tests.
- Typed in-process events between modules — event-driven thinking without distribution complexity.
- Auto-generated module documentation and dependency diagrams — useful portfolio artifacts.
- If future scale requires extraction, each module can become a service with bounded scope.

### Negative (costs we explicitly accept)
- Small learning curve for Spring Modulith.
- Scaling is vertical first; horizontal scaling of individual modules requires extraction work later.
- No process isolation — a bug in one module can affect the whole JVM.

### Neutral / Open Questions
- Which modules emerge from domain modeling? (To be defined.)
- Single database with one schema, single database with schema-per-module, or database-per-module? (Defer to a future ADR after domain modeling.)
- Synchronous in-process events or asynchronous (with a transactional outbox)? (Defer.)

## References
- Spring Modulith reference: https://docs.spring.io/spring-modulith/reference/
- "MonolithFirst" — Martin Fowler
- "The Majestic Monolith" — DHH
- Shopify engineering blog on the modular monolith
