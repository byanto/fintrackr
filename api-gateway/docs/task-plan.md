# Fintrackr Api Gateway: Task Plan (Actionable Breakdown)

*Here is the breakdown of tasks for each epic. Tasks that are completed or partially completed are marked as `[x]`.*

---

### Epic 1: Foundational Gateway Setup `(COMPLETED)`

*   **Story:** As a developer, I need a basic gateway application that can register itself with the service registry.
    *   `[x]` Initialize a new Spring Boot project with Maven.
    *   `[x]` Add dependencies for Spring Cloud Gateway and Eureka Client.
    *   `[x]` Configure `application.yml` to set the service name and connect to the Eureka server.

---

### Epic 2: Dynamic Route Configuration `(COMPLETED)`

*   **Story:** As a developer, I need to configure the gateway to dynamically route requests to downstream services based on their registered names.
    *   `[x]` Configure routes in `GatewayConfig.java` for `user-service` and `investment-service`.
    *   `[x]` Use the `lb://` (load balancer) protocol to enable service discovery-based routing.
    *   `[x]` Define path predicates for each route (e.g., `/api/auth/**` -> `user-service`, `/api/investments/**` -> `investment-service`).

---

### Epic 3: Centralized Reactive Security `(COMPLETED)`

*   **Story:** As a developer, I need to secure service endpoints by integrating with Spring Security to validate JWTs at the gateway level.
    *   `[x]` Add `spring-boot-starter-security` dependency.
    *   `[x]` Create a `JwtService.java` class to handle JWT parsing and validation logic.
    *   `[x]` Create the `AuthenticationConverter.java` class to extract the bearer token from the request header.
    *   `[x]` Create the `JwtAuthenticationManager.java` to validate the token and populate the `Authentication` object with the user's principal and roles.
    *   `[x]` Create `SecurityConfig.java` to define the security filter chain.
        *   `[x]` Disable CSRF, Form Login, and HTTP Basic authentication.
        *   `[x]` Define public vs. authenticated routes using `authorizeExchange`.
        *   `[x]` Wire up the `AuthenticationWebFilter` with the custom converter and manager.
    *   `[x]` Remove the old manual `AuthenticationFilter` and `RouterValidator`.
    *   `[x]` Update `GatewayConfig.java` to remove the manual filter from the routes.
    *   `[x]` Write integration tests for the security flow to verify:
        *   Public routes are accessible without a token.
        *   Secured routes are blocked without a token.
        *   Secured routes are blocked with an invalid/expired token.
        *   Secured routes are accessible with a valid token.

---

### Epic 4: Gateway Hardening & Observability `(PENDING)`

*   **Story:** As a developer, I need proper logging and health checks for the gateway.
    *   `[ ]` Add Spring Boot Actuator dependency.
    *   `[ ]` Configure Actuator to expose `health`, `info`, and `gateway` endpoints.
    *   `[ ]` Implement structured logging to trace requests as they pass through the gateway.
    *   `[ ]` Add correlation IDs to track a single request across the gateway and downstream services.

*   **Story:** As an operator, I need to protect the system from traffic spikes and downstream service failures.
    *   `[ ]` Add `spring-cloud-starter-circuitbreaker-resilience4j` dependency.
    *   `[ ]` Wrap the `user-service` route in a Circuit Breaker filter in `GatewayConfig.java`.
    *   `[ ]` Configure the Circuit Breaker properties (e.g., failure rate threshold) in the centralized config server.
    *   `[ ]` Implement a default rate-limiting filter using the built-in `RequestRateLimiter` GatewayFilterFactory.
    *   `[ ]` Write an integration test to verify that the circuit breaker opens when the downstream service is unavailable.

---

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

---

### Epic 7: Centralized Configuration `(PENDING)`

*   **Story:** As a developer, I need the gateway to receive its configuration from the central config server.
    *   `[ ]` Add the `spring-cloud-starter-config` dependency.
    *   `[ ]` Create a `bootstrap.yml` file to point the gateway to the `config-server`.
    *   `[ ]` Move existing gateway configuration (e.g., JWT secret, logging levels) to a new `api-gateway.yml` file in the `config-repo` Git repository.
