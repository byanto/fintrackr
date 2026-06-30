# ADR-009: Pure Domain Model with a Separate JPA Persistence Model

- **Status:** Accepted
- **Date:** 2026-06-01
- **Deciders:** Budi Yanto

## Context

Fintrackr persists its domain with JPA/Hibernate (chosen for v1 for its ecosystem maturity, feature completeness, and the breadth of community and tooling support that eases long-term maintenance — see the persistence-technology decision). That choice forces a question deferred from the value-object ADRs: **does the domain model double as the JPA persistence model (merge), or do they remain two separate models with a mapper between them (separate)?**

The forcing function is a structural conflict between the domain we have already designed and what JPA/Hibernate requires:

- The domain leans on Java 21 records and immutability. `Money`, `Quantity`, `Percentage`, `Symbol` are records (ADR-007, ADR-008); `Transaction` is a sealed interface with record subtypes and `Acquisition` is immutable on creation (ADR-004, ADR-005).
- Standard JPA cannot persist these as entities. An `@Entity` must be a non-final, mutable class with a no-arg constructor and field access for proxying and dirty-checking; a `record` satisfies none of these. (Records *can* serve as Hibernate `@Embeddable` value types via the canonical constructor since Hibernate 6.2 — but that covers value objects only, never entities.)

So merging would mean dismantling the records, the sealed `Transaction` hierarchy, and the immutability guarantees — discarding the Java 21 design that ADR-005/008 deliberately adopted as a correctness mechanism and a clarity choice. A decision is required now, before the first `@Entity` and the first repository adapter are written, because it determines the shape of every persistence class and of the repository ports.

## Decision

Keep the **domain model pure** and persist it through a **separate JPA persistence model**, with an explicit mapper between the two, contained entirely within the outbound persistence adapter.

Concretely:

- **Domain layer** (`domain/`): pure Java. No `jakarta.persistence` and no Spring annotations. Records, sealed types, and immutability are preserved exactly as designed in ADR-004/005/007/008.
- **Persistence model** (`infrastructure/adapter/out/persistence/`): mutable `@Entity` classes (`PortfolioJpaEntity`, `AcquisitionJpaEntity`, `BrokerAccountJpaEntity`, and an `@Inheritance` hierarchy for the transaction ledger), plus columns or `@Embeddable` types for value objects. These classes exist only to satisfy Hibernate and never leak past the adapter.
- **Mapper**: **MapStruct** (compile-time, type-safe; the standard tool for bean mapping in this ecosystem) converts domain ↔ persistence inside the adapter. Because value objects construct through factory methods rather than public constructors, the mapper uses small custom mapping methods (e.g. `default Money toMoney(BigDecimal v) { return Money.of(v); }`) — which is also what keeps ADR-007's normalization running on reads. The repository **port** (`application/port/out`) speaks only domain types; the JPA adapter implements it and maps at the boundary. (MapStruct and Lombok are both annotation processors and require `lombok-mapstruct-binding` with correct processor ordering, or MapStruct cannot see Lombok-generated accessors.)
- **Value objects** map field-for-field to columns (or persistence-side embeddables for multi-field VOs). Reconstruction always goes through the domain factory methods (`Money.of(...)`, etc.), so ADR-007's scale/rounding normalization runs again at the read boundary — the checkpoint is preserved on rehydration, not just on first construction.
- **Sealed `Transaction` mapping**: the domain sealed hierarchy maps to a JPA single-table (or joined) `@Inheritance` hierarchy on the persistence side. MapStruct handles subtype dispatch via `@SubclassMapping`; because that is weaker on exhaustiveness than a `switch` over a sealed type (a forgotten subtype is not reliably a compile-time error), this one hierarchy may instead retain a small hand-written exhaustive `switch` so that adding a new transaction variant fails to compile until it is handled. Decided when the mapper is implemented.

Hexagonal structure is unaffected: the repository remains an outbound port; only the persistence adapter knows that JPA exists.

## Alternatives Considered

- **(A) Merge — the domain entities *are* the JPA entities.** Mutable `@Entity` classes, mutation funnelled through aggregate methods, no public setters; value objects as `@Embeddable`.
  - Pros: no mapper; far less code; it is the mainstream Hibernate practice; faster to ship.
  - Cons: forces abandoning the sealed-record `Transaction` hierarchy and `Acquisition` immutability — the exact Java 21 design ADR-005/008 adopted as a deliberate correctness and clarity aid. Imports persistence concerns into the domain; "immutable on creation" degrades from a compiler guarantee to a discipline convention (protected setters for Hibernate).
  - Why rejected (for Fintrackr): the sealed/record design is a deliberate part of the model's correctness and clarity. The cost of separation is bounded here — Fintrackr has essentially one rich aggregate (Portfolio, with its acquisitions, allocations, and ledger); BrokerAccount is small and Asset Catalog is in-memory in v1 — so the mapper is not pervasive ceremony.

- **(B) Drop records; make the domain mutable, JPA-friendly classes.** A variant of merge that targets records specifically.
  - Pros: makes vanilla, spec-compliant JPA straightforward.
  - Cons: discards the compact-constructor normalization checkpoint (ADR-007) and value-object immutability/equality for no benefit that separation does not already provide.
  - Why rejected: strictly worse than keeping records, since factory-method reconstruction in the mapper (or records-as-embeddable on the persistence side) preserves both persistence and the guarantees.

- **(C) Spring Data JDBC instead of JPA.** Natively fits immutable records and the aggregate-as-unit model, dissolving most of the mapping.
  - Pros: cleanest architectural fit; least ceremony; strong "right tool for the model" narrative.
  - Cons: Spring Data JDBC lacks JPA features (automatic lazy loading, first-level cache, batch operations, boot-time query validation) and has a smaller ecosystem and narrower adoption than Hibernate.
  - Why rejected: JPA's ecosystem maturity and feature breadth outweigh the architectural elegance for v1. Recorded as a viable future option.

## Consequences

### Positive
- The domain keeps its full Java 21 design — records, sealed `Transaction`, immutability — untouched by persistence. ADR-004/005/007/008 hold as written.
- Persistence is a genuine swappable detail behind an outbound port; the domain has zero compile-time dependency on Hibernate.
- ADR-007 normalization is enforced on reads as well as writes, because the mapper rebuilds value objects through their factory methods.
- The sealed `Transaction` hierarchy lets the mapping boundary enforce exhaustiveness: with a hand-written `switch`, "added a transaction type but forgot to persist it" becomes a compile error rather than a runtime surprise (a guarantee `@SubclassMapping` alone does not give).

### Negative (costs we explicitly accept)
- Two models for the same concepts plus mapping to maintain — MapStruct generates the mechanical part, but the two models and their mapping config are still real surface, concentrated in the Portfolio aggregate. Accepted because it is bounded and it protects the domain design.
- Mapping the `@OneToMany` and inheritance relationships in the persistence model is the fiddly part of JPA; this is where the Hibernate learning (and the bugs) will concentrate. Covered by Testcontainers integration tests (ADR-002).
- A change to a domain shape requires touching the persistence entity and the mapper too — a deliberate, visible cost rather than a silent one.

### Neutral / Open Questions
- Persistence-side use of Hibernate's record-`@Embeddable` support (6.2+) for multi-field value objects is available but optional; decide per value object when implementing. The domain records themselves stay annotation-free regardless.
- Column types for persisted `Money` (`NUMERIC(19, 0)` vs `BIGINT`) remain open per ADR-007; decided when the schema is written.
- **(Resolved)** Mapping is done with **MapStruct**. Open sub-decision: whether the sealed `Transaction` dispatch uses MapStruct's `@SubclassMapping` or a hand-written exhaustive `switch` — decided when the mapper is built.
- Petalytics will deliberately use the merge approach (A) for comparison; lessons feed back here.

## References
- ADR-001 — Modular monolith
- ADR-002 — Testing strategy (Testcontainers for persistence integration tests)
- ADR-004 — Transaction ledger as source of truth
- ADR-005 — Specific Identification cost basis (sealed `Transaction`, immutable `Acquisition`)
- ADR-007 — Currency scale and rounding policy (the normalization checkpoint)
- ADR-008 — `Quantity` and `Percentage` value objects (records)
- Fintrackr Domain Model (`03-fintrackr-domain-model.md`)
- Hibernate ORM 6.2+ record-as-`@Embeddable` support (`EmbeddableInstantiator`, canonical-constructor instantiation)
- MapStruct — compile-time bean mapping (`@SubclassMapping`, factory-method mapping, `lombok-mapstruct-binding`)
