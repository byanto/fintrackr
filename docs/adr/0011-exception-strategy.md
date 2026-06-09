# ADR-011: Exception Strategy — Technical vs Domain Exceptions

- **Status:** Accepted
- **Date:** 2026-06-08
- **Deciders:** Budi Yanto

## Context

Building the first value object (`Money`) raised a concrete question — when a guard fails (null amount, null currency), throw a generic JDK exception or a custom domain exception? — that generalises to the whole codebase.

The system produces two very different kinds of failure: low-level **precondition violations** (null, malformed primitive) that mean the *caller has a bug*, and **business-rule violations** (insufficient trading balance, oversell, dividend dated before a lot opened) that are *meaningful domain outcomes*. A consistent, documented policy is needed before exceptions proliferate ad hoc and before the REST boundary must map failures to responses. ADR-007 already drew a related line: `Money` does minimal guarding, while reasonableness/business checks live in aggregate methods.

## Decision

- **Technical / precondition violations → standard JDK exceptions.** Null arguments use `NullPointerException` (via `Objects.requireNonNull`); other malformed-but-non-null arguments use `IllegalArgumentException`. These signal programmer error. No custom types for these.
- **Domain / business-rule violations → custom unchecked domain exceptions.** A base `DomainException extends RuntimeException`, with subtypes named in the ubiquitous language (e.g. `InsufficientTradingBalanceException`). Thrown from aggregate methods, never from value-object technical guards.
- Domain exceptions are **unchecked** — checked exceptions pollute domain signatures and couple callers.
- The shared `DomainException` base lets a single boundary handler (`@RestControllerAdvice`) map the whole family to structured responses (RFC 9457 `ProblemDetail`). Boundary mapping is deferred until the REST adapter exists; the base type is introduced when the first aggregate rule needs it.
- **Dividing test:** "would a business stakeholder recognise this as a meaningful outcome?" Yes → domain exception. "The programmer passed nonsense" → JDK exception.
- **Tests assert on exception type** (`isInstanceOf`), and at most `hasMessageContaining`; never exact message strings.

## Alternatives Considered

- **Option A — custom exceptions for everything, including null guards.**
    - Pros: uniform.
    - Cons: ceremony around programmer bugs; a `NullAmountException` adds nothing over NPE.
    - Why rejected: over-engineering; bloats the type count for non-domain failures.

- **Option B — generic JDK exceptions for everything, including business rules.**
    - Pros: zero custom types.
    - Cons: loses domain language; makes boundary mapping a type/string-sniffing mess; business outcomes aren't first-class or catchable by type.
    - Why rejected: business rules deserve to be modelled.

- **Option C — checked domain exceptions.**
    - Pros: compiler-visible failure modes.
    - Cons: signature noise; forced propagation through application services.
    - Why rejected: modern practice favours unchecked for domain errors.

- **Option D — a single catch-all domain exception, no subtypes.**
    - Pros: minimal.
    - Cons: loses per-rule mapping to specific responses/messages.
    - Why rejected long-term: base + specific subtypes is the balance. We start with the base and add subtypes as rules appear (YAGNI on subtypes).

## Consequences

### Positive
- A clear, teachable line, consistent with ADR-007's value-object/aggregate split.
- Domain exceptions are part of the ubiquitous language and map cleanly at the boundary.
- Type-based tests are robust to message rewording.

### Negative (costs we explicitly accept)
- A margin judgement remains for value-object *domain-format* violations (e.g. `Symbol`'s regex, `Percentage`'s 0..1 range): technical or domain? Deferred to when those VOs are built.
- Unchecked exceptions are invisible in signatures — failure modes rely on docs/tests, not the compiler.

### Neutral / Open Questions
- Exact `ProblemDetail` mapping (status codes, error codes, i18n) — when the REST adapter is built.
- Whether VO format violations join the domain hierarchy — open.
- Where `DomainException` lives — likely a small cross-cutting home (possibly `shared`, though it is behaviour, not a value object) — decided when introduced.

## References
- ADR-002 (testing — assert by type)
- ADR-007 (`Money` — minimal VO guarding; reasonableness at the aggregate)
- ADR-010 (shared module — candidate home for `DomainException`)
- RFC 9457 — Problem Details for HTTP APIs
- Eric Evans, *Domain-Driven Design* — exceptions in the ubiquitous language