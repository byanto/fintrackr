# Fintrackr User Service: Task Plan (Actionable Breakdown)

Here is the breakdown of tasks for each epic. Tasks we have already completed are marked as [x].

---

## Epic 1: Foundational Service Setup (COMPLETED)

Story: As a developer, I need a basic Spring Boot project that can connect to the service registry.
[x] Initialize a new Spring Boot project with Spring Initializr.
[x] Add dependencies for Web, JPA, PostgreSQL, and Lombok.
[x] Add the Eureka Client dependency.
[x] Configure `application.yml` to set the service name and connect to the Eureka server.

---

## Epic 2: Core User Identity (COMPLETED)

Story: As a developer, I need to define and persist the core user entity.
[x] Create the `User` JPA entity with fields: `id`, `username`, `email`, `password`, `createdAt`.
[x] Create the `UserRepository` interface extending `JpaRepository`.
[x] Implement a database migration script (e.g., using Flyway or Hibernate's ddl-auto) to create the `users` table.

---

## Epic 3: Authentication & Security (COMPLETED)

Story: As a user, I want to register a new account and log in to receive an authentication token.

[x] Add Spring Security dependency.
[x] Configure `SecurityConfig` to define public endpoints (`/auth/**`) and secure all others.
[x] Implement `PasswordEncoder` bean (BCrypt).
[x] Create `AuthenticationController` with `/register` and `/login` endpoints.
[x] Create `AuthenticationService` to handle the business logic for registration (hashing passwords) and login.
[x] Create `JwtService` to generate access and refresh tokens.
[x] Implement `JwtAuthenticationFilter` to validate tokens on incoming requests and set the `SecurityContext`.
[x] Implement `UserDetailsService` to load user data for Spring Security.

Story: As a user, I want to use a refresh token to get a new access token without logging in again.

[x] Add a `/renewtoken` endpoint to `AuthenticationController`.
[x] Implement the token renewal logic in `JwtService` and `AuthenticationService`.

---

## Epic 4: User Profile Management

Story: As an authenticated user, I want to retrieve my own profile information. (COMPLETED)
[x] Create `UserController` with a protected `/api/users/me` endpoint.
[x] Implement `UserService` with a `getUserByUsername` method.
[x] Create `UserResponse` DTO to avoid exposing the `User` entity directly.
[x] Write a `@WebMvcTest` for the `UserController`.

Story: As an authenticated user, I want to update my email address. (PENDING)
[ ] Add a `PUT /api/users/me` endpoint to `UserController`.
[ ] Create a `UpdateUserRequest` DTO with validation (e.g. `@Email`)
[ ] Implement the update logic in `UserService`, ensuring a user can only update their own data.
[ ] Write integration tests for the update flow.

---

## Epic 5: Service Hardening & Observability

Story: As a developer, I need to ensure my time-based logic is fully testable and reliable. (COMPLETED)
[x] (Lesson Learned) Refactor `JwtService` to remove dependency on `System.currentTimeMillis()`.
[x] (Lesson Learned) Create a `ClockConfig` to provide a `java.time.Clock` bean.
[x] (Lesson Learned) Inject the `Clock` bean into `JwtService`.
[x] (Lesson Learned) Write a full `AuthenticationFlowIntegrationTest` using `@MockitoBean` for the `Clock` to prove the flaky test is fixed and the flow is 100% reliable.

Story: As a developer, I need proper logging and health checks for the service. (PENDING)
[ ] Add Spring Boot Actuator dependency.
[ ] Configure Actuator to expose `health` and `info` endpoints.
[ ] Implement structured logging (e.g., using Logstash/JSON format) to make logs easier to parse.
[ ] Add correlation IDs to track a single request across the service.

---

## Epic 6: API Documentation & Developer Experience
Story: As a developer, I need clear and interactive API documentation to understand how to use the user-service endpoints. (PENDING)
[x] Add the `springdoc-openapi-starter-webmvc-ui` dependency to `pom.xml`.
[x] Configure Spring Security to permit public access to the Swagger UI and API docs endpoints (`/swagger-ui.html`, `/v3/api-docs/**`).
[x] Configure global OpenAPI info in `application.yml` (title, description, version).
[x] Annotate all Controller methods and DTOs with `@Operation`, `@ApiResponse`, and `@Schema` to provide clear examples and descriptions.
[x] Verify that the generated documentation correctly reflects security requirements (e.g., shows the "lock" icon on protected endpoints).

---

### Epic 7: Containerization `(COMPLETED)`
*   **Story:** As a developer, I need to build and run the service as a container for consistent deployments.
    *   `[x]` Add `jib-maven-plugin` to the parent POM to build container images without a Dockerfile.
    *   `[x]` Configure the plugin in the service's `pom.xml` to expose the correct container port.
    *   `[x]` Add a service entry to `docker-compose.yml` to orchestrate the container during local development.
