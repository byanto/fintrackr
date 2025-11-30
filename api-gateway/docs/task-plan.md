# Fintrackr Api Gateway: Task Plan (Actionable Breakdown)

*Tasks that are completed or partially completed are marked as `[x]`.*

---

### Epic 1: Foundational Gateway Setup `(COMPLETED)`
*   **Story:** As a developer, I need a basic gateway application that can register itself with the service registry.
    *   `[x]` Initialize a new Spring Boot project with Maven.
    *   `[x]` Add dependencies for Spring Cloud Gateway and Eureka Client.
    *   `[x]` Configure `application.yml` to set the service name and connect to the Eureka server.

---

### Epic 2: Dynamic Route Configuration `(COMPLETED)`
*   **Story:** As a developer, I need to configure the gateway to dynamically route requests to downstream services based on their registered names.
    *   `[x]` Configure routes in `application.yml` for `user-service` and `investment-service`.
    *   `[x]` Use the `lb://` (load balancer) protocol to enable service discovery-based routing.
    *   `[x]` Define path predicates for each route (e.g., `/api/auth/**` -> `user-service`, `/api/investments/**` -> `investment-service`).

---

### Epic 3: Centralized Authentication Filter `(IN PROGRESS)`
*   **Story:** As a developer, I need to secure service endpoints by validating JWTs at the gateway level.
    *   `[x]` Create the `AuthenticationConverter.java` class.
    *   `[ ]` **(Next Task)** Create a `RouterValidator.java` class to define which endpoints are public (e.g., `/auth/login`, `/auth/register`) and which are secured.
    *   `[ ]` **(Next Task)** Create a `JwtService.java` class to handle JWT parsing and validation logic, using the shared JWT secret.
    *   `[ ]` **(Next Task)** Inject `RouterValidator` and `JwtUtil` into the `AuthenticationFilter`.
    *   `[ ]` **(Next Task)** Complete the filter logic to:
        *   Check if the route is secured.
        *   Extract the `Authorization` header.
        *   Validate the JWT using `JwtUtil`.
        *   Return an `UNAUTHORIZED` status on failure.
    *   `[ ]` **(Next Task)** Implement the request enrichment logic to add user headers (`X-Authenticated-User-Username`) upon successful validation.
    *   `[ ]` **(Next Task)** Apply the `AuthenticationFilter` to the appropriate routes in the `application.yml` configuration.
    *   `[ ]` **(Next Task)** Write integration tests for the gateway to verify:
        *   Public routes are accessible without a token.
        *   Secured routes are blocked without a token.
        *   Secured routes are blocked with an invalid/expired token.
        *   Secured routes are accessible with a valid token, and the downstream service receives the enriched headers.

---

### Epic 4: Gateway Hardening & Observability `(PENDING)`
*   **Story:** As a developer, I need proper logging and health checks for the gateway.
    *   `[ ]` Add Spring Boot Actuator dependency.
    *   `[ ]` Configure Actuator to expose `health`, `info`, and `gateway/routes` endpoints.
    *   `[ ]` Implement structured logging to trace requests as they pass through the gateway.
    *   `[ ]` Add correlation IDs (e.g., using Spring Cloud Sleuth) to track a single request across the gateway and downstream services.

*   **Story:** As an operator, I need to protect the system from traffic spikes.
    *   `[ ]` Implement a default rate-limiting filter using the built-in `RequestRateLimiter` gateway filter factory.

### Epic 5: Unified API Documentation `(PENDING)`
*   **Story:** As a developer, I need a single place to view the API documentation for all Fintrackr services.
    *   `[ ]` **(Next Task)** Add the `springdoc-openapi-starter-webflux-ui` dependency to the gateway's `pom.xml`.
    *   `[ ]` **(Next Task)** Configure `application.yml` to discover and group the OpenAPI definitions from downstream services (e.g., `user-service`).

---

### Epic 6: Containerization `(COMPLETED)`
*   **Story:** As a developer, I need to build and run the gateway as a container for consistent deployments.
    *   `[x]` Inherit `jib-maven-plugin` from the parent POM to build container images.
    *   `[x]` Add a service entry to `docker-compose.yml` to orchestrate the container.
    *   `[x]` Configure the exposed port in `docker-compose.yml` to be the main entry point for the system.
    *   `[ ]` **(Next Task)** Ensure the gateway's security configuration allows public access to the Swagger UI and its resources.
    *   `[ ]` **(Next Task)** Verify that the aggregated documentation correctly displays endpoints from `user-service` with their proper security definitions.
