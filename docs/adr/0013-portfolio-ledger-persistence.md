# ADR-013: Portfolio Ledger Persistence: Single Table with Per-Type Constraints

- **Status:** Accepted
- **Date:** 2026-10-13
- **Deciders:** Budi Yanto

## Context

`Portfolio` is the first aggregate with child collections: the append-only ledger
(`List<Transaction>`, a sealed hierarchy, ADR-004/005) and `List<Acquisition>`. ADR-009 settled
that domain and persistence models are separate and that the mapper dispatches over `Transaction`
with a hand-written exhaustive `switch`. It did not settle how a sealed hierarchy maps to tables,
how child rows are appended, or what happens when a second module needs a persistence-side value
class.

v1 has five transaction types (Buy, Sell, Dividend, Deposit, Withdrawal); two exist today. Every
`findById` reads the whole ledger of one portfolio, all types together. The ledger is append-only:
a stored row is never updated or deleted. The most important rule for a ledger row is that it has
exactly one type and the data of that type.

## Decision

- **Single table.** One `transactions` table with the shared columns (`id`, `portfolio_id`,
  `date`, `type`) and the columns of every type, nullable. A CHECK limits `type` to the types the
  code knows (today `DEPOSIT` and `BUY`). A `seq` identity column records the order in which rows
  were entered. It is never reused, so a deleted and reinserted row would come back with a new
  `seq`.
- **Per-type CHECK constraints in both directions.** For each type: the columns it requires are
  NOT NULL, and the columns of the other types are NULL. A CHECK expression that evaluates to NULL
  passes, so the column-level CHECK on quantity scale does not make quantity required; only the
  per-type CHECK does.
- **Java mapping with `@Inheritance(strategy = SINGLE_TABLE)`.** An abstract `TransactionJpaEntity`
  holds the shared columns, one subclass per type holds only the fields that type has. `type` is
  mapped once, as the `@DiscriminatorColumn`.
- **Explicit `@DiscriminatorValue` on every subclass.** The stored value is a contract with the
  rows already written. Hibernate's default is the entity name, which would let a class rename
  change the stored value. Renaming a stored value is a data migration (update the rows, replace
  the CHECK) in the same change as the Java code.
- **Foreign key to `broker_accounts`.** `portfolios.broker_account_id` references the Brokerage
  table. This couples the two modules' schemas under the same condition as ADR-003's single
  transaction: one database while the system is a monolith. Spring Modulith does not see SQL, so
  this ADR is the only record of that coupling.
- **Associations.** Unidirectional `@OneToMany` from `PortfolioJpaEntity` with
  `@JoinColumn(name = "portfolio_id", nullable = false, updatable = false)`, `cascade = PERSIST`
  only. No `REMOVE`, no `ALL`, no `orphanRemoval`: removing an element from a list must never
  delete a ledger row. Both collections stay lazy. `updatable = false` stops Hibernate from sending
  an `UPDATE ... SET portfolio_id` per child row after the INSERT that already wrote the column.
  They are `List`s. The ledger loads ordered by `date, seq`, acquisitions by
  `open_date, id`. Backdated entries are allowed, so the date alone is not the recording order.
- **Appending.** On save of an existing portfolio the adapter loads the managed entity, builds a
  `HashSet` of the ids already in each collection, and appends every domain transaction and
  acquisition whose id is not in it. Existing rows are never modified: child entities have no
  setters, and `seq` is neither insertable nor updatable. This relies on the domain assigning ids
  (`TransactionId.generate()`, `AcquisitionId.generate()`).
- **No `repository.save` for an existing portfolio.** `merge` walks the collections, and without
  cascade MERGE it resolves a new child with an assigned id as an existing row, which fails with
  `EntityNotFoundException`. Dirty checking and cascade PERSIST write the change at flush. A new
  portfolio still goes through `repository.save`, where the null version selects `persist`
  (ADR-009).
- **`acquisition_id`** has a foreign key to `acquisitions` and a UNIQUE constraint: one buy opens
  one acquisition. PostgreSQL treats NULLs as distinct, so the many rows without an acquisition do
  not collide.
- **Quantity columns** are unconstrained `NUMERIC` with `CHECK (scale(col) IN (0, 4))`. `Quantity`
  records whether it counts shares or units only in its scale (ADR-008), and `NUMERIC(p, s)` would
  rewrite that scale on the way in. The mapper rebuilds quantities with `Quantity.reconstitute`,
  which keeps value and scale as stored and skips the creation rules. Those rules ran when the
  entry was recorded; normalizing on load would turn a stored `5.0000` into `5`. The scale CHECK
  keeps any other scale out.
- **Child factories.** `Deposit`, `Buy` and `Acquisition` have `create`, which generates the id,
  and `reconstitute`, which takes the stored one. The mapper only uses `reconstitute`.
- **Child entities** set every field in one constructor and have no setters. ADR-009's "no
  all-args constructor" rule exists because an aggregate's mutable fields are copied onto a
  loaded entity. A ledger row is never updated, so one constructor is the clearer shape.
- **Optimistic locking on the root only.** Only `portfolios` has a `version` column. Appending
  to a collection bumps the root's version, so two concurrent saves of one portfolio conflict
  even when each only adds a row.
- **Both adapter methods are `@Transactional`** (`findById` read-only). The mapper reads lazy
  collections after `repository.findById` returns, and the update path relies on dirty checking,
  which only sees a managed entity. Inside a use case they join its transaction (ADR-003);
  without one they open their own. `@DataJpaTest` hides a missing annotation because the test
  transaction keeps the session open, so one test runs without a surrounding transaction.
- **`fk_transactions_acquisition_id` is checked immediately**, not DEFERRABLE. Hibernate inserts
  acquisitions before transactions only because it cascades in alphabetical attribute order;
  `acquisitionId` is a plain UUID, so Hibernate does not know about the dependency. A test that
  saves a buy guards the order, and a comment on the collection fields says why.
- **`MoneyEmbeddable`** is duplicated in `portfolio.adapter.out.persistence`. This is the second
  consumer ADR-009 anticipated; per-module duplication is chosen because the class has no
  behaviour. An infrastructure module is reconsidered when an embeddable gains behaviour.

## Alternatives Considered

- **Joined tables with exclusive subtypes.** A `transactions` base table with `type` and
  `UNIQUE (id, type)`; one table per type with a fixed type (`DEFAULT` plus `CHECK`) and
  `FOREIGN KEY (id, type) REFERENCES transactions (id, type)`.
  - Pros: per-type rules are plain NOT NULL columns, readable at a glance.
  - Cons: exclusivity needs the composite key construction in every type table; a base row without
    any type row is still possible, since plain constraints cannot express "at least one"; every
    load joins the base table to every type table; every entry is two INSERTs.
  - This was the first decision and the first version of the migration. Rejected before the
    migration was merged: the construction is a lot of schema for a rule single table holds by
    construction, it still leaves a gap, and the dominant read is all types together.
- **Joined tables without a type column, or with a type column in the base table only.**
  - Rejected: without a type column a base row can have two type rows and Hibernate silently
    returns one of them. A type column in the base table alone is a second source of truth that
    can disagree with the type tables.
- **Table per class.**
  - Pros: no NULLs, no joins for a single type.
  - Cons: reading all types needs UNION; shared columns repeated; nothing can reference "any
    transaction".
  - Rejected: the dominant read is all types together.
- **Single table mapped to one flat entity with a type enum (`@Enumerated(EnumType.STRING)`).**
  - Pros: one entity class instead of a hierarchy; the mapper's `switch` over the enum is
    exhaustive in the read direction too, so a new type without mapping code does not compile.
  - Cons: every entity instance carries the fields of every type, NULL by convention; the Java
    model does not show which fields belong to which type.
  - Rejected: subclasses that carry only their own fields were preferred. The per-type CHECKs and
    a round-trip test per type cover what the compiler no longer checks.
- **No foreign key to `broker_accounts`.**
  - Pros: no schema coupling across modules.
  - Cons: nothing in v1 checks that a portfolio's broker account exists; adding the key later
    requires cleaning orphan rows first.
  - Rejected for v1. Dropping the key when Brokerage moves to its own database is one statement.
- **Bidirectional association, or child-to-parent only.**
  - Rejected: the aggregate is always loaded from the root and children are never queried alone.
    A back reference would be unused code with the `toString`/`hashCode` recursion risk ADR-009
    warns about. Child-to-parent only would need repositories for the children, which breaks the
    rule that an aggregate has one repository.
- **`MoneyEmbeddable` shared from Brokerage, moved to `shared`, or flat columns.**
  - Rejected: the Brokerage class is in an internal package (Modulith); `shared` must not depend on
    `jakarta.persistence` (ArchUnit); flat columns would let a mapper pair one Money's amount with
    another's currency without a compile error, across six Money fields.
- **Rebuild the collections on every save (clear, re-add, `orphanRemoval = true`).**
  - Rejected: in one flush Hibernate fails with `EntityExistsException`. With a flush in between
    it deletes and reinserts every row with new `seq` values, so the journal is rewritten on every
    save. Corrections are reversal entries, never deletes.
- **`repository.save` in both branches, as ADR-009 prescribes for flat aggregates.**
  - Rejected: fails on the first append (see Decision). Cascade MERGE would make it work, but
    merge would then copy every loaded child on every save and SELECT each new one first.
- **`DEFERRABLE INITIALLY DEFERRED` on `fk_transactions_acquisition_id`.**
  - Pros: insert order no longer matters.
  - Cons: a violation surfaces only at commit. `@DataJpaTest` rolls back, so a test would never
    see it unless it flushes and sets the constraints to immediate by hand.
  - Rejected: a fast, loud failure is worth more than independence from the insert order.
- **`Set` instead of `List` for the collections.**
  - Rejected: a `Set` of entities needs `equals`/`hashCode` on the entity, with the
    hash-before-flush trap ADR-009 warns about. A `List` plus a `HashSet<UUID>` of loaded ids gives
    the same lookup without it.

## Consequences

### Positive
- A ledger row has exactly one type by construction, and its type data sits in the same row.
- The schema rejects a row with missing data for its type, a row with data of another type, a buy
  without its asset, two buys opening the same acquisition, and a portfolio pointing at a missing
  broker account.
- Loading a portfolio reads the ledger with one SELECT, without joins. Each entry is one INSERT.
- Deleting a portfolio with history is refused by the database.
- Switching association style later needs no data migration; the schema is the same.

### Negative (costs we explicitly accept)
- Per-type rules are CHECK expressions, harder to read than NOT NULL columns, and each new type
  extends every per-type CHECK.
- The table is wide and most columns are NULL in any row.
- Two parallel hierarchies: domain records and entity classes. The `switch` over entity subclasses
  on the read side is not exhaustive; a forgotten type fails at runtime, not at compile time.
- The quantity's kind is stored as a scale, a representation carrying meaning.
- Insert order depends on Hibernate's alphabetical cascade order. Renaming either collection
  field can break every save of a buy; only a test notices.
- The `Portfolio` adapter departs from ADR-009's "call save in both cases". A reader of ADR-009
  alone gets the wrong rule for aggregates with collections.

### Neutral / Open Questions
- Ledger rows are immutable through the Java mapping, not the database. A trigger rejecting
  UPDATE and DELETE on `transactions` would make it a schema rule (ADR-004).
- Whether the read side can be checked by the compiler without a sealed entity hierarchy.
- Integration tests start with an empty database, so they do not prove that rows written before a
  migration are still readable after it.
- When `Sell` lands: if feat(portfolio): persist the portfolio aggregate through a JPA adapter`remainingQuantity` is stored, acquisition rows are no longer append-only
  and the adapter needs an update path for them.
- When the asset model (ADR-006) exists, the quantity's kind should come from the asset.
- The aggregate loads the whole ledger. If that becomes slow, the fix is in the aggregate (load
  without history, read the ledger separately), not in the mapping.

## References
- ADR-003 (cross-aggregate cash invariant, one transaction)
- ADR-004 (ledger as source of truth, append-only)
- ADR-006 (asset families)
- ADR-008 (`Quantity` scale)
- ADR-009 (domain/persistence separation, entity shape, per-adapter embeddables, optimistic locking)
- ADR-010 (shared kernel promotion rule)
- Vaughn Vernon, *Implementing Domain-Driven Design*, aggregate design
- PostgreSQL documentation: numeric types, constraints
- Hibernate ORM user guide: inheritance
