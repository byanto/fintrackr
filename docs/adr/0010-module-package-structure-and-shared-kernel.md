# ADR-010: Module Package Structure and the Shared Kernel

- **Status:** Accepted
- **Date:** 2026-06-08
- **Deciders:** Budi Yanto

## Context

ADR-001 chose a modular monolith with Spring Modulith but explicitly left open *which* modules emerge from domain modelling and how packages are laid out inside them. Domain modelling (`domain-model.md`) settled three bounded contexts: Portfolio Management, Brokerage, Asset Catalog.

Writing the first domain code (the `Money` value object) forces three answers now:
1. What the top-level modules are and how they are named.
2. How packages are laid out inside each module.
3. Where value objects used by *more than one* context live — because placing a shared VO inside one context's module creates a cross-module dependency, and the wrong direction (e.g. Brokerage → Portfolio merely to reuse `Money`) violates the dependency directions fixed in ADR-003 (Portfolio Management → Brokerage, never the reverse).

Spring Modulith enforces module boundaries at build time, so this layout is not cosmetic: it determines which dependencies are legal.

## Decision

- **Three context modules** as direct sub-packages of the application root: `portfolio`, `brokerage`, `catalog`. `catalog` is the short form of "Asset Catalog" and names the context's role (a reference-data registry), consistent with `portfolio` and `brokerage` naming areas rather than central entities.
- **Hexagonal layout inside each context module:** `domain/model` (entities, value objects), `domain/...` (services, events as they appear), `application/port`, `infrastructure/adapter/...`.
- **A fourth module, `shared`, holds the Shared Kernel:** value objects genuinely used by more than one context. v1 membership is **`Money` and `AssetId` only**. `Quantity` lives in `portfolio` (sole user); `Percentage` lives in `brokerage` (fee rates only). The kernel is kept deliberately minimal.
- **`shared` is laid out flat** (value objects directly under it), NOT mirroring `domain/model`. A kernel has a single concern (value types) and no application/infrastructure tiers; the hexagonal nesting would advertise layers that will never exist there.
- **`shared` is realized as a Spring Modulith open module** (`@ApplicationModule(type = OPEN)`) and/or registered globally (`@Modulithic(sharedModules = "shared")`), so every context may depend on it even once explicit allowed-dependency whitelists are introduced.
- **Dependency directions** (per ADR-003) are unchanged: Portfolio Management → Brokerage and → Asset Catalog through published APIs; never the reverse. `shared` is depended upon by all and depends on nothing.

## Alternatives Considered

- **Option A — put shared VOs in the core (`portfolio`).**
    - Pros: no extra module.
    - Cons: forces Brokerage/Catalog → Portfolio dependencies, reversing ADR-003 and coupling supporting contexts to the core.
    - Why rejected: wrong dependency direction; couples supporting subdomains to the core.

- **Option B — duplicate the VOs per context.**
    - Pros: full module autonomy.
    - Cons: `Money`/`Symbol` are identical everywhere; duplication yields non-interoperable types (a Brokerage `Money` cannot be compared to a Portfolio `Money`) and drift risk.
    - Why rejected: duplication is a service-boundary technique, not for in-process modules sharing an identical concept.

- **Option C — a `common`/`utils` module instead of a curated `shared` kernel.**
    - Pros: familiar.
    - Cons: an unconstrained "common" becomes a junk drawer and a high-fan-in coupling magnet with no entry bar.
    - Why rejected: `shared`, treated as a kernel with a curated membership and a high entry bar, names intent and resists creep.

- **Option D — mirror `shared/domain/model` for consistency.**
    - Pros: uniform navigation across all modules.
    - Cons: empty pass-through packages advertising tiers that never materialise.
    - Why rejected: the consistency benefit is weak against the misleading-structure cost; the kernel's single concern justifies a flat layout.

- **Option E — put `Quantity`/`Percentage` in `shared` too.**
    - Pros: one place for all value objects.
    - Cons: each is used by exactly one context; sharing over-exposes single-context types and invites accidental cross-context use.
    - Why rejected: keep the kernel to what is *actually* shared.

## Consequences

### Positive
- Dependency directions stay clean and Spring-Modulith-enforceable; the kernel has zero outbound dependencies.
- The kernel's minimalism (two VOs) keeps coupling low and the "shared = commitment" discipline visible.
- A flat `shared` honestly reflects what it is — no misleading empty layers.
- Extraction-friendly: if a context is later split into a service, the kernel is already isolated (copy it, or publish it as a contract).

### Negative (costs we explicitly accept)
- A shared kernel is, by definition, the highest-coupling area: changes to `Money`/`Symbol` ripple to all contexts. Mitigated by keeping it tiny and stable.
- `shared` is laid out differently from the context modules — a deliberate, honest inconsistency.

### Neutral / Open Questions
- Whether `shared` uses `type = OPEN`, `@Modulithic(sharedModules)`, or per-module `allowedDependencies` — decided when the `ApplicationModules.verify()` test is added (not mutually exclusive).
- A future net-worth / `CashAccount` abstraction (ADR-006) does NOT belong in `shared` if it lands — it is a read-model concern.

## References
- ADR-001 (modular monolith — "which modules emerge" was left open; answered here)
- ADR-003 (cross-context dependency directions)
- ADR-006 (asset/cash family split)
- ADR-008 (`Quantity`/`Percentage` — why they are single-context)
- Spring Modulith reference — shared/open modules, `@ApplicationModule`, `@Modulithic`
- Eric Evans, *Domain-Driven Design* — Shared Kernel pattern