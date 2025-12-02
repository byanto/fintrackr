# Fintrackr Investment Service: Implementation Plan

---

## 1. Project Vision & Goals

The `investment-service` is responsible for managing all data related to a user's financial investments. It will track portfolios, broker accounts, financial instruments, and log all trades to provide an accurate, real-time view of a user's holdings.

---

## 2. Core Features (Epics)

The work will be broken down into the following high-level epics:

- **Epic 1: Foundational Service Setup:** Establish the basic Spring Boot project, database connectivity, and service discovery registration.
- **Epic 2: Core Data Management:** Implement the master data management for `BrokerAccount` and `Instrument`.
- **Epic 3: Portfolio & Trade Management:** Implement the core logic for managing `Portfolio`s and logging immutable `Trade` events.
- **Epic 4: Holdings Calculation:** Implement the business logic to process trades and update the `Holding` materialized view.
- **Epic 5: Security & Authorization:** Implement resource server security to ensure users can only access their own data.
- **Epic 6: API Documentation:** Implement SpringDoc to generate and expose the service's OpenAPI specification.
- **Epic 7: Service Hardening & Observability:** Ensure the service is robust, testable, and production-ready.
- **Epic 8: Containerization:** Package the application as a container image for consistent deployment.
- **Epic 9: Centralized Configuration Management:** Move all service configurations to a central, version-controlled server.
- **Epic 10: Resilient Inter-Service Communication:** Implement fault-tolerant communication for any future calls to other services.

---

## 3. Key Design Decisions

- **Domain-Driven Design (DDD):** The service is structured around the core business domain. The database schema clearly defines aggregates like `Portfolio`, `Instrument`, `BrokerAccount`, `Trade`, and `Holding`.
- **Event-Sourcing Pattern:** The use of an immutable `Trade` table as an event log, which is then used to calculate the current state in the `Holding` table, is a core design principle. This provides a full audit history.
-   **Database Schema Management:** The database schema is managed through version-controlled SQL scripts using Flyway, ensuring consistent and repeatable database state across all environments.
- **Stateless & Secure:** The service is a stateless resource server. It trusts the `api-gateway` for authentication and relies on enriched headers (e.g., `X-Authenticated-User-Id`) for authorization.
- **Testability First:** All code must be written with testability in mind.
- **API Documentation:** All endpoints must be documented using OpenAPI 3 (SpringDoc).
- **Version-Controlled Migrations:** Database schema changes must be managed via Flyway migration scripts. Hibernate's `ddl-auto` will be set to `validate`.
- **Fault Tolerance:** The service must be designed to handle failures in downstream services gracefully using patterns like Circuit Breakers.
- **Configuration as Code:** All service configurations will be managed centrally in a Git repository, promoting consistency and auditability.
