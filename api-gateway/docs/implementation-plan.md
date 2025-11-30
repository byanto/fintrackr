# Fintrackr API Gateway: Implementation Plan

---

## 1. Project Context Summary

*   **Project Vision:** The `api-gateway` is the single, unified entry point for all external requests into the Fintrackr microservices ecosystem. Its primary goals are to route traffic to the appropriate downstream services, provide a centralized layer for cross-cutting concerns like security, and simplify the client-side interface.
*   **Core Architecture:** A reactive, non-blocking Spring Cloud Gateway application. It acts as a reverse proxy and is a client of the `service-registry`.
*   **Key Design Decisions:**
    *   **Centralized Authentication:** The gateway is responsible for validating JWTs for all incoming requests to secured services. It simplifies downstream services by handling authentication centrally.
    *   **"Spring-Native" Security:** It implements a proper reactive security flow using a custom `ReactiveAuthenticationManager` and `ServerAuthenticationConverter` to validate JWTs and populate the `SecurityContext`.
    *   **Dynamic Routing:** It uses the `service-registry` (Eureka) to dynamically discover and route to service instances via the `lb://` protocol, avoiding hardcoded locations.
    *   **Programmatic Configuration:** Routes are defined in `GatewayConfig.java` and security rules in `SecurityConfig.java`, providing a clear and powerful separation of concerns.
*   **Technology Stack:** Java 21, Spring Boot 3, Spring Cloud Gateway, Eureka Client, JJWT, Project Reactor.

---

## 2. Core Features (Epics)

*   **Epic 1: Foundational Gateway Setup:** Establish the basic Spring Cloud Gateway project and connect it to the service registry.
*   **Epic 2: Dynamic Route Configuration:** Configure routes to downstream services (`user-service`, `investment-service`) using service discovery.
*   **Epic 3: Centralized Reactive Security:** Implement a "Spring-Native" security flow to validate JWTs and enable role-based access control.
*   **Epic 4: Gateway Hardening & Observability:** Add logging, health checks, and potentially rate limiting and circuit breaking to make the gateway robust and production-ready.
*   **Epic 5: Unified API Documentation:** Configure the gateway to aggregate and display OpenAPI (Swagger) documentation from all downstream services.
*   **Epic 6: Containerization:** Package the application as a container image for consistent deployment.