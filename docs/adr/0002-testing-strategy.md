# ADR-002: Testing Strategy for Fintrackr

- **Status:** Accepted
- **Date:** 2026-05-24
- **Deciders:** Budi Yanto

## Context

Fintrackr deals with money. Bugs cost trust and require careful audit. The codebase has multiple layers — pure domain logic, application services, REST adapters, JPA persistence — and they don't all benefit from the same testing approach.

Strict TDD ("red-green-refactor on every line") has high cognitive overhead and is awkward for code that is mostly framework wiring (controller mappings, JPA entities). Test-after with no discipline produces incomplete coverage and misses TDD's design pressure on domain logic.

Rigorous, automated testing is essential here. Being deliberate about *when* TDD pays off — and when it does not — is part of a coherent strategy rather than dogma.

A coherent, defensible testing strategy must be established before the codebase grows.

## Decision

Adopt a layered testing strategy:

- **Domain layer:** strict TDD (red-green-refactor). All aggregates, value objects, domain services. This is where calculation correctness and invariant enforcement live — the highest-value place for TDD.
- **Application services & ports:** test-after, unit-tested with mocks for adapter ports.
- **Adapters (REST, JPA):** test-after using Spring's test slices (`@WebMvcTest`, `@DataJpaTest`) and Testcontainers for real-database integration tests.
- **End-to-end / black box:** a small number of "happy path" tests through the public API.

Follow the **test pyramid**: many unit tests, fewer integration tests, very few end-to-end. No coverage target — coverage incentivizes meaningless tests. Instead, every public method on a domain aggregate must have tests for at least: happy path, each invariant violated, and any edge case implied by the domain.

Stack:
- **JUnit 5 (Jupiter)** — test framework
- **AssertJ** — fluent assertions (preferred over Hamcrest or plain JUnit)
- **Mockito** — used sparingly; mock collaborators, not the system under test
- **Testcontainers** — real PostgreSQL for repository and integration tests
- **Spring Boot test slices** — for adapter testing without full context

## Alternatives Considered

- **Strict TDD everywhere.** Rejected. High overhead on framework-heavy code (controllers, JPA mappings) where TDD does not improve design. Slows progress for marginal benefit.
- **Test-after everywhere.** Rejected. Loses TDD's design pressure on domain logic. Tests written after the code tend to confirm what the code does rather than challenge it. Easier to forget edge cases.
- **100% line-coverage target.** Rejected. Incentivizes gaming metrics — tests that exercise code without asserting meaningful behavior. Coverage is a leading indicator, not a goal.
- **BDD with Cucumber/Gherkin.** Rejected for v1. Significant tooling overhead, real value only when non-developer stakeholders write or read specs. I am both developer and stakeholder.

## Consequences

### Positive
- Domain layer becomes solid and regression-resistant — exactly where bugs are most costly.
- TDD on domain forces explicit thinking about edge cases and invariants before writing code.
- Test pyramid keeps test suite fast and feedback loop short.

### Negative (costs we explicitly accept)
- Requires judgment about which layer a piece of code belongs to. Sometimes ambiguous.
- Adapter tests may discover problems later than TDD would have. Accepted because adapters are mostly framework wiring with low logical complexity.
- No coverage gate in CI initially. Could be added later if quality slips.

### Neutral / Open Questions
- When do property-based tests (jqwik) earn their keep? Likely the cost-basis and P&L calculations.
- When do we add mutation testing (PIT)? Probably after v1 if time allows.
- Performance tests deferred until a real performance concern arises.

## References
- *Growing Object-Oriented Software, Guided by Tests* — Freeman & Pryce
- *Effective Software Testing* — Aniche
- Test pyramid — Martin Fowler
- Spring Boot testing reference: https://docs.spring.io/spring-boot/reference/testing/
