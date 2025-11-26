# Fintrackr API Gateway: Implementation Plan

---

## 1. Project Context Summary

*   **Project Vision:** The `api-gateway` is the single, unified entry point for all external requests into the Fintrackr microservices ecosystem. Its primary goals are to route traffic to the appropriate downstream services, provide a centralized layer for cross-cutting concerns like security, and simplify the client-side interface.
*   **Core Architecture:** A reactive, non-blocking Spring Cloud Gateway application. It acts as a reverse proxy and is a client of the `service-registry`.
*   **Key Design Decisions:**
    *   **Centralized Authentication:** The gateway is responsible for validating JWTs for all incoming requests to secured services. It simplifies downstream services by handling authentication centrally.
    *   **Request Enrichment:** After validating a token, the gateway enriches the request by adding user-identity headers (e.g., `X-Authenticated-User-Username`) before forwarding it. Downstream services can trust these headers implicitly.
    *   **Dynamic Routing:** It uses the `service-registry` (Eureka) to dynamically discover and route to service instances via the `lb://` protocol, avoiding hardcoded locations.
    *   **Configuration-driven:** Routes and filters are primarily managed via `application.yml` for clarity and ease of maintenance.
*   **Technology Stack:** Java 21, Spring Boot 3, Spring Cloud Gateway, Eureka Client, JJWT, Project Reactor.

---

## 2. Core Features (Epics)

*   **Epic 1: Foundational Gateway Setup:** Establish the basic Spring Cloud Gateway project and connect it to the service registry.
*   **Epic 2: Dynamic Route Configuration:** Configure routes to downstream services (`user-service`, `investment-service`) using service discovery.
*   **Epic 3: Centralized Authentication Filter:** Implement a custom filter to intercept requests, validate JWTs, and reject unauthorized access.
*   **Epic 4: Gateway Hardening & Observability:** Add logging, health checks, and potentially rate limiting and circuit breaking to make the gateway robust and production-ready.
*   **Epic 5: Unified API Documentation:** Configure the gateway to aggregate and display OpenAPI (Swagger) documentation from all downstream services.
*   **Epic 6: Containerization:** Package the application as a container image for consistent deployment.