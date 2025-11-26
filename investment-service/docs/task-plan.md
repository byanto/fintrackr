# Fintrackr Investment Service: Task Plan (Actionable Breakdown)

*Tasks that are likely completed based on project structure are marked as `[x]`.*

---

### Epic 1: Foundational Service Setup `(COMPLETED)`
*   **Story:** As a developer, I need a basic service that can connect to its database and the service registry.
    *   `[x]` Initialize a new Spring Boot project with Maven.
    *   `[x]` Add dependencies for Web, JPA, Flyway, PostgreSQL, Lombok, and Eureka Client.
    *   `[x]` Configure `application.yml` to set the service name and connect to the database and Eureka.
    *   `[x]` Implement Flyway migration scripts (`V1`, `V2`) to create the initial database schema.

---

### Epic 2: Core Data Management `(PENDING)`
*   **Story:** As a user, I need to manage my brokerage accounts and the financial instruments I trade.
    *   `[ ]` **(Next Task)** Create JPA entities for `BrokerAccount` and `Instrument` based on the Flyway schema.
    *   `[ ]` **(Next Task)** Create repositories, services, DTOs, mappers, and controllers for `BrokerAccount` CRUD operations.
    *   `[ ]` **(Next Task)** Create repositories, services, DTOs, mappers, and controllers for `Instrument` CRUD operations.

---

### Epic 3: Portfolio & Trade Management `(PENDING)`
*   **Story:** As a user, I need to organize my investments into portfolios and record my buy/sell transactions.
    *   `[ ]` Create JPA entities for `Portfolio` and `Trade`.
    *   `[ ]` Establish the relationship between `Portfolio` and `BrokerAccount`.
    *   `[ ]` Create repositories, services, DTOs, mappers, and controllers for `Portfolio` CRUD operations.
    *   `[ ]` Create a service and controller endpoint to add a new `Trade` event for a given `Portfolio` and `Instrument`. This endpoint will be the primary input for the system.

---

### Epic 4: Holdings Calculation `(PENDING)`
*   **Story:** As a user, I need to see my current holdings for each instrument in my portfolio.
    *   `[ ]` Create the `Holding` JPA entity.
    *   `[ ]` Create the `HoldingRepository`.
    *   `[ ]` Implement a `HoldingService` with logic that is triggered whenever a `Trade` is created.
    *   `[ ]` This service will:
        *   Find the existing `Holding` for the portfolio/instrument pair.
        *   Recalculate the `quantity` and `average_price`.
        *   Save the updated `Holding` record.
    *   `[ ]` Create a controller with endpoints to view current holdings for a portfolio.

---

### Epic 5: Security & Authorization `(PENDING)`
*   **Story:** As a developer, I need to ensure that users can only access their own investment data.
    *   `[ ]` Add Spring Security dependency.
    *   `[ ]` Configure `SecurityConfig` to secure all endpoints.
    *   `[ ]` Add a `userId` field to the `BrokerAccount` or `Portfolio` entities to associate them with a Fintrackr user.
    *   `[ ]` Implement logic in all services to use the `userId` from the request header (`X-Authenticated-User-Id`) to authorize data access.
    *   `[ ]` **(Next Task)** Write integration tests to verify that a request for User A's data fails if User B's identity is passed in the header.

---

### Epic 6: API Documentation `(PENDING)`
*   **Story:** As a developer, I need the `investment-service` to publish its API documentation.
    *   `[ ]` Add the `springdoc-openapi-starter-webmvc-ui` dependency.
    *   `[ ]` Annotate all Controller methods and DTOs with `@Operation`, `@ApiResponse`, and `@Schema`.
    *   `[ ]` Configure global OpenAPI info in `application.yml` (title, description).

---

### Epic 7: Service Hardening & Observability `(PENDING)`
*   **Story:** As a developer, I need proper logging and health checks for the service.
    *   `[x]` Add Spring Boot Actuator dependency (already in `pom.xml`).
    *   `[ ]` Configure Actuator to expose `health` and `info` endpoints.
    *   `[ ]` Implement structured logging and add correlation IDs to trace requests.

---

### Epic 8: Containerization `(COMPLETED)`
*   **Story:** As a developer, I need to build and run the service as a container for consistent deployments.
    *   `[x]` Add `jib-maven-plugin` to the parent POM to build container images without a Dockerfile.
    *   `[x]` Configure the plugin in the service's `pom.xml` to expose the correct container port.
    *   `[x]` Add a service entry to `docker-compose.yml` to orchestrate the container during local development.
