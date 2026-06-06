# Fintrackr

Fintrackr is a personal investment tracking backend built to consolidate portfolios across multiple broker accounts.  

## The Problem

For over 8 years, I managed my investments using fragmented Google Sheets. With assets scattered across different brokers, getting a true consolidated view of my cost basis, realized profits, and per lot dividend allocations became too painful and prone to error. I needed a reliable system to track exactly what I own and how it performs. Fintrackr is the engineering answer to that real-world problem.

## Features and Scope

Fintrackr focuses strictly on core portfolio management:
- Tracks cash movements (deposits, withdrawals) and asset transactions (buys, sells, dividends).
- Calculates specific identification cost basis and attributes dividends to individual acquisition lots.
- Maintains a single source of truth using an append-only transaction ledger.

Scope boundaries for v1:
- Single user (personal use only)
- Single currency (IDR only, no foreign exchange handling)
- Manual data entry (no automated broker API integrations yet) 

## Architecture

The system is built as a modular monolith using Spring Modulith, applying hexagonal architecture and Domain-Driven Design (DDD) principles.

Key decisions are recorded as ADRs in the docs/adr folder:
- [ADR-001: Modular Monolith over Microservices](docs/adr/0001-modular-monolith.md)
- [ADR-002: Testing Strategy](docs/adr/0002-testing-strategy.md)
- [ADR-003: Cross-aggregate Cash Invariant](docs/adr/0003-cross-aggregate-cash-invariant.md)
- [ADR-004: Transaction Ledger as Source of Truth](docs/adr/0004-transaction-ledger-source-of-truth.md)
- [ADR-005: Specific Identification Cost Basis](docs/adr/0005-specific-identification-cost-basis.md)
- [ADR-006: Asset Model Split](docs/adr/0006-asset-model-and-family-split.md)
- [ADR-007: Currency Scale and Rounding Policy](docs/adr/0007-currency-scale-and-rounding-policy.md)
- [ADR-008: Quantity and Percentage Value Objects](docs/adr/0008-quantity-and-percentage-value-objects.md)
- [ADR-009: Pure Domain Model with Separate JPA Persistence](docs/adr/0009-domain-persistence-separation.md)

Domain model is recorded in the docs/domain folder:
- [Domain Model v1](docs/domain/domain-model.md)

## Tech Stack

- Language: Java 21
- Framework: Spring Boot 3, Spring Modulith
- Persistence: PostgreSQL, Hibernate/Spring Data JPA, Flyway
- Mapping: MapStruct
- Testing: JUnit 5, AssertJ, Mockito, Testcontainers
  
## Getting Started

You only need JDK 21 and Docker (for Testcontainers) installed. The project uses the Maven Wrapper, so you do not need to install Maven locally.

Run the test suite:
```
./mvnw test
```

Start the application:
```
./mvnw spring-boot:run
```

## Status
Work in progress (v1).