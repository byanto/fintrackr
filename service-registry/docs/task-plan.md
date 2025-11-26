# Fintrackr Service Registry: Task Plan (Actionable Breakdown)

*Tasks that are completed are marked as `[x]`.*

---

### Epic 1: Foundational Server Setup `(COMPLETED)`
*   **Story:** As a developer, I need a running service registry so that other microservices can find each other.
    *   `[x]` Initialize a new Spring Boot project with Maven.
    *   `[x]` Add the `spring-cloud-starter-netflix-eureka-server` dependency.
    *   `[x]` Add the `@EnableEurekaServer` annotation to the main application class.
    *   `[x]` Configure `application.yml` to:
        *   Set the server port to `8761`.
        *   Set the application name to `service-registry`.
        *   Disable client-side registration and fetching (`register-with-eureka: false`, `fetch-registry: false`).

---

### Epic 2: Server Hardening & High Availability `(PENDING)`
*   **Story:** As an operator, I need to monitor the health and status of the service registry.
    *   `[ ]` **(Next Task)** Add Spring Boot Actuator dependency.
    *   `[ ]` **(Next Task)** Configure Actuator to expose `health` and `info` endpoints.
    *   `[ ]` **(Next Task)** Implement structured logging to monitor registration events and server status.

*   **Story:** As an operator, I need the service registry to be highly available to prevent system-wide outages.
    *   `[ ]` **(Future Task)** Research and document the configuration for a peer-aware, multi-instance Eureka setup.
    *   `[ ]` **(Future Task)** Create a `docker-ha` or `prod` Spring profile in `application.yml` to define peer URLs.
    *   `[ ]` **(Future Task)** Update `docker-compose.yml` with a second `service-registry-peer` service to demonstrate the high-availability configuration.

---

### Epic 3: Containerization `(COMPLETED)`
*   **Story:** As a developer, I need to build and run the service as a container for consistent deployments.
    *   `[x]` Add `jib-maven-plugin` to the parent POM to build container images without a Dockerfile.
    *   `[x]` Configure the plugin in the service's `pom.xml` to expose the correct container port.
    *   `[x]` Add a service entry to `docker-compose.yml` to orchestrate the container during local development.
