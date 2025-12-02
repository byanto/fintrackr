# Fintrackr User Service: Task Plan (Actionable Breakdown)

*Here is the breakdown of tasks for each epic. Tasks that are completed or partially completed are marked as `[x]`.*

---

## Epic 1: Foundational Service Setup `(COMPLETED)`

*   **Story:** As a developer, I need a basic Spring Boot project that can connect to the service registry.
    *   `[x]` Initialize a new Spring Boot project with Spring Initializr.
    *   `[x]` Add dependencies for Web, JPA, PostgreSQL, and Lombok.
    *   `[x]` Add the Eureka Client dependency.
    *   `[x]` Configure `application.yml` to set the service name and connect to the Eureka server.

---

## Epic 2: Core User Identity `(COMPLETED)`

*   **Story:** As a developer, I need to define and persist the core user entity using version-controlled database migrations.
    *   `[x]` Create the `User` JPA entity with fields: `id`, `username`, `email`, `password`, `createdAt`.
    *   `[x]` Create the `UserRepository` interface extending `JpaRepository`.
    *   `[x]` Add the `flyway-core` and `flyway-database-postgresql` dependencies to the `pom.xml`.
    *   `[x]` Disable Hibernate's automatic `ddl-auto` in `application.yml` to give Flyway full control.
    *   `[x]` Create the initial migration script `V1__Create_users_table.sql` in `src/main/resources/db/migration`.

---

## Epic 3: Authentication & Security `(COMPLETED)`

*   **Story:** As a user, I want to register a new account and log in to receive an authentication token.
    *   `[x]` Add Spring Security dependency.
    *   `[x]` Configure `SecurityConfig` to define public endpoints (`/auth/**`) and secure all others.
    *   `[x]` Implement `PasswordEncoder` bean (BCrypt).
    *   `[x]` Create `AuthenticationController` with `/register` and `/login` endpoints.
    *   `[x]` Create `AuthenticationService` to handle the business logic for registration (hashing passwords) and login.
    *   `[x]` Create `JwtService` to generate access and refresh tokens.
    *   `[x]` Implement `JwtAuthenticationFilter` to validate tokens on incoming requests and set the `SecurityContext`.
    *   `[x]` Implement `UserDetailsService` to load user data for Spring Security.

*   **Story:** As a user, I want to use a refresh token to get a new access token without logging in again.
    *   `[x]` Add a `/renewtoken` endpoint to `AuthenticationController`.
    *   `[x]` Implement the token renewal logic in `JwtService` and `AuthenticationService`.

---

## Epic 4: User Profile Management `(COMPLETED)`

*   **Story:** As an authenticated user, I want to retrieve my own profile information.
    *   `[x]` Create `UserController` with a protected `/api/users/me` endpoint.
    *   `[x]` Implement `UserService` with a `getUserByUsername` method.
    *   `[x]` Create `UserResponse` DTO to avoid exposing the `User` entity directly.
    *   `[x]` Write a `@WebMvcTest` for the `UserController`.

*   **Story:** As an authenticated user, I want to update my email address.
    *   `[x]` Add a `PUT /api/users/me` endpoint to `UserController`.
    *   `[x]` Create a `UpdateUserRequest` DTO with validation (e.g. `@Email`)
    *   `[x]` Implement the update logic in `UserService`, ensuring a user can only update their own data.
    *   `[x]` Write integration tests for the update flow.

---

## Epic 5: Service Hardening & Observability `(IN PROGRESS)`

*   **Story:** As a developer, I need to ensure my time-based logic is fully testable and reliable. (COMPLETED)
    *   `[x]` (Lesson Learned) Refactor `JwtService` to remove dependency on `System.currentTimeMillis()`.
    *   `[x]` (Lesson Learned) Create a `ClockConfig` to provide a `java.time.Clock` bean.
    *   `[x]` (Lesson Learned) Inject the `Clock` bean into `JwtService`.
    *   `[x]` (Lesson Learned) Write a full `AuthenticationFlowIntegrationTest` using `@MockitoBean` for the `Clock` to prove the flaky test is fixed and the flow is 100% reliable.

*   **Story:** As a developer, I need proper logging and health checks for the service. (PENDING)
    *   `[x]` Add Spring Boot Actuator dependency.
    *   `[x]` Configure Actuator to expose `health` and `info` endpoints.
    *   `[ ]` Implement structured logging (e.g., using Logstash/JSON format) to make logs easier to parse.
    *   `[ ]` Add correlation IDs to track a single request across the service.

---

## Epic 6: API Documentation & Developer Experience `(COMPLETED)`

*   **Story:** As a developer, I need clear and interactive API documentation to understand how to use the user-service endpoints.
    *   `[x]` Add the `springdoc-openapi-starter-webmvc-ui` dependency to `pom.xml`.
    *   `[x]` Configure Spring Security to permit public access to the Swagger UI and API docs endpoints (`/swagger-ui.html`, `/v3/api-docs/**`).
    *   `[x]` Configure global OpenAPI info in `application.yml` (title, description, version).
    *   `[x]` Annotate all Controller methods and DTOs with `@Operation`, `@ApiResponse`, and `@Schema` to provide clear examples and descriptions.
    *   `[x]` Verify that the generated documentation correctly reflects security requirements (e.g., shows the "lock" icon on protected endpoints).

---

### Epic 7: Containerization `(COMPLETED)`

*   **Story:** As a developer, I need to build and run the service as a container for consistent deployments.
    *   `[x]` Add `jib-maven-plugin` to the parent POM to build container images without a Dockerfile.
    *   `[x]` Configure the plugin in the service's `pom.xml` to expose the correct container port.
    *   `[x]` Add a service entry to `docker-compose.yml` to orchestrate the container during local development.

---

## Epic 8: Centralized Configuration Management `(PENDING)`

*   **Story:** As a developer, I want to manage all service configurations in a single, version-controlled location to simplify updates and improve consistency.
    *   `[ ]` Create a new `config-repo` Git repository to store `.yml` files for all services.
    *   `[ ]` Create a new `config-server` Spring Boot module.
    *   `[ ]` Add `spring-cloud-config-server` dependency to the config server.
    *   `[ ]` Configure the config server to connect to the `config-repo` Git repository.
    *   `[ ]` Add the config server to `docker-compose.yml`.
    *   `[ ]` Update all other services (`user-service`, `api-gateway`, etc.) to use `spring-cloud-starter-config` and connect to the config server.
    *   `[ ]` Migrate existing properties from local `application.yml` files to the centralized repository.

---

## Epic 9: Resilient Inter-Service Communication `(PENDING)`

*   **Story:** As a developer, I need services to communicate with each other in a clean, declarative, and fault-tolerant way.
    *   `[ ]` Add `spring-cloud-starter-openfeign` dependency to services that need to call others (e.g., `investment-service`).
    *   `[ ]` Create a Feign client interface (e.g., `UserClient`) to define methods for calling the `user-service` API.
    *   `[ ]` Enable Feign clients in the main application class.
    *   `[ ]` Add `spring-cloud-starter-circuitbreaker-resilience4j` dependency.
    *   `[ ]` Configure Resilience4j properties in the centralized config repository (e.g., timeout durations, failure rate thresholds).
    *   `[ ]` Wrap a Feign client call in a `@CircuitBreaker` annotation.
    *   `[ ]` Implement a fallback method to provide a default response when the circuit breaker is open.
    *   `[ ]` Write an integration test to verify the circuit breaker and fallback behavior when a downstream service is unavailable.
