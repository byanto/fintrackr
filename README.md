# Fintrackr

A personal investment-tracking backend that consolidates portfolios across
multiple broker accounts into one consistent view.

## The Problem

For over 8 years, I managed my investments using fragmented Google Sheets.
With assets scattered across different brokers, getting a true consolidated
view of my cost basis, realized profits, and per-lot dividend allocations
became too painful and prone to error. I needed a reliable system to track
exactly what I own and how it performs. Fintrackr is the engineering answer
to that real-world problem.

## Features and Scope

Fintrackr focuses strictly on core portfolio management:
- Records every cash and asset movement: deposits, withdrawals, buys, sells, dividends.
- Calculates cost basis by specific identification and attributes dividends
  to individual acquisition lots.
- Treats an append-only transaction ledger as the single source of truth;
  holdings are a derived, cached view.

Scope boundaries for v1:
- Single user (personal use only)
- Single currency (IDR only, no foreign exchange handling)
- Manual data entry (no automated broker API integrations yet)

## Architecture

A modular monolith built with Spring Modulith, applying hexagonal
architecture and Domain-Driven Design inside each module. Three bounded
contexts: **Portfolio Management** (acquisitions, sells, dividends, holdings),
**Brokerage** (broker accounts, cash balance, fees), and **Asset Catalog**
(reference data). Module boundaries are enforced by a Modulith verification
test, not by convention.

The full model lives in [Domain Model v1](docs/domain/domain-model.md).

Key decisions are recorded as ADRs in [docs/adr](docs/adr):
- [ADR-001: Modular Monolith over Microservices](docs/adr/0001-modular-monolith.md)
- [ADR-002: Testing Strategy](docs/adr/0002-testing-strategy.md)
- [ADR-003: Cross-aggregate Cash Invariant](docs/adr/0003-cross-aggregate-cash-invariant.md)
- [ADR-004: Transaction Ledger as Source of Truth](docs/adr/0004-transaction-ledger-source-of-truth.md)
- [ADR-005: Specific Identification Cost Basis](docs/adr/0005-specific-identification-cost-basis.md)
- [ADR-006: Asset Model Split](docs/adr/0006-asset-model-and-family-split.md)
- [ADR-007: Currency Scale and Rounding Policy](docs/adr/0007-currency-scale-and-rounding-policy.md)
- [ADR-008: Quantity and Percentage Value Objects](docs/adr/0008-quantity-and-percentage-value-objects.md)
- [ADR-009: Pure Domain Model with Separate JPA Persistence](docs/adr/0009-domain-persistence-separation.md)
- [ADR-010: Module Package Structure and Shared Kernel](docs/adr/0010-module-package-structure-and-shared-kernel.md)
- [ADR-011: Exception Strategy](docs/adr/0011-exception-strategy.md)
- [ADR-012: Asset Identity via ISIN](docs/adr/0012-asset-identity-via-isin.md)

## Tech Stack

- Language: Java 21
- Framework: Spring Boot 3, Spring Modulith
- Persistence: PostgreSQL, Hibernate/Spring Data JPA, Flyway
- Mapping: MapStruct
- Testing: JUnit 5, AssertJ, Mockito, Testcontainers

## Getting Started

You need JDK 21 and Docker (tests and dev-time runs use Testcontainers).
The Maven Wrapper handles the rest.

The project is built domain-first, test-first — the test suite is currently
the best way to see it work:

```
./mvnw test
```

Start the application with a throwaway PostgreSQL container (Spring Boot
dev-time Testcontainers support):

```
./mvnw spring-boot:test-run
```

There is no REST API yet. The web and persistence layers arrive once the
domain model is complete (see Status below).

## Status

Work in progress (v1), built in vertical slices:

- [x] Domain model and ADRs
- [x] Core value objects: Money, Quantity, Percentage, AssetId (ISIN)
- [x] Portfolio aggregate: deposits and buys, test-driven
- [ ] Brokerage aggregate and fee computation
- [ ] Sells with acquisition-selection strategies (FIFO, LIFO, manual, ...)
- [ ] Dividends with per-lot attribution
- [ ] Persistence layer (JPA + Flyway)
- [ ] REST API
- [ ] Minimal web UI
