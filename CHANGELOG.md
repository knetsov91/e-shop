# Changelog

No tagged releases yet, so this is organized by development phase instead of version number. Newest first.

## Order ownership & admin bootstrap (2026-09)

### Added
- `userId` on `OrderCommand` and `OrderQuery`, captured from the JWT subject at order creation — the prerequisite for order ownership (role-based "my orders" vs. an admin/analytics "all orders" view is separate follow-up work, pending a JWT role claim)
- `AdminBootstrapRunner` in user-service — creates the first admin account from `ADMIN_USERNAME`/`ADMIN_EMAIL`/`ADMIN_PASSWORD` on startup if none exists yet, since public registration always assigns `ROLE_USER` by design and there was previously no way to obtain an admin account at all

### Fixed
- **`GET /api/v1/orders` returned nothing against real data — a functional deficit, not a documented limitation.** The endpoint existed, was tested, and looked complete, but every write path (order creation, `PaymentEventConsumer`, `InventoryEventConsumer`) only ever touched `OrderCommand` (PostgreSQL); nothing wrote to `OrderQueryRepository` (MongoDB) at all. All three now also write/update the `OrderQuery` projection.
- `OrderQuery`'s `@Id` was `jakarta.persistence.Id`, which Spring Data MongoDB doesn't recognize — `orderId` was never actually mapped to the document's `_id`, so `findById(orderId)` could never have matched anything even once documents existed. Fixed to `org.springframework.data.annotation.Id`.

## Payment service & saga completion (2026-07 – 2026-08)

### Added
- `eshop-payment-service` module (Stripe SDK, dev profile, Consul registration)
- `PaymentRequestedEvent` / `PaymentEvent` and Kafka producer/consumer wiring between order-service and payment-service
- `PaymentRequestConsumer` in payment-service, with retry and dead-letter topic handling
- `PaymentEventConsumer` in order-service to move orders through the saga on payment success/failure
- Outbox pattern for order-service (`OutboxEvent`, `OutboxEventRepository`, `OutboxEventRelay`) — replaces direct Kafka publish on order creation with a transactional outbox + scheduled relay
- `OrderStatus` enum to model saga states explicitly instead of ad-hoc strings
- ADR-002 documenting the order saga coordination strategy

### Fixed
- Order creation flow updated to publish a payment request instead of an order-created event, closing a gap where orders could be marked complete before payment was confirmed

## Traefik rate limiting & load testing (2026-08)

### Added
- Rate-limit middleware defined via Traefik's file provider and attached to every service router through Consul tags
- Gatling load-test subproject (`eshop-load-tests`) with a `RateLimitSimulation` to verify the limiter under load

## Observability: Sentry (2026-06 – 2026-07)

### Added
- Sentry Gradle plugin (auto-instrumentation) added to all four services
- Error and log monitoring configured per service, including capture of key domain events (order placed/updated, product created, stock reserved/insufficient) and DLT messages

## Inventory service & idempotent event handling (2026-05 – 2026-06)

### Added
- `eshop-inventory-service` with `Stock` entity (optimistic locking) and `InventoryService.reserveStock`
- `OrderEventConsumer` to consume `order-events` from Kafka, with retry + dead-letter topic
- `InventoryEvent` publishing back to order-service after stock reservation
- `ProcessedOrderEvent` table to make `reserveStock` idempotent against duplicate Kafka deliveries — skips re-publishing `inventory-events` for messages already processed

### Fixed
- Duplicate `order-events` deliveries no longer double-decrement stock (root cause of a bug found via a real-infrastructure Testcontainers test simulating redelivery)

## Auth & security (2026-06)

### Added
- User-service configured as an OAuth2 Authorization Server with RSA JWK and registered clients (service + web, OIDC support)
- Order-service configured as an OAuth2 resource server, validating JWTs against the JWK endpoint
- `UserService`/`UserRepository`/`RegisterRequest` — registration endpoint with bcrypt password encoding

### Fixed
- CSRF checks disabled on user-service (SameSite cookies already prevent cross-site requests)
- Service client secret moved from hardcoded value to environment variable
- OAuth2 endpoints registered in Consul tags so Traefik actually routes to them

## Centralized configuration & CI (2026-05)

### Added
- Consul KV used as the shared config source for Kafka, Sentry, and management endpoint settings across all services, replacing duplicated per-service YAML
- Per-service server port, JPA, and Kafka overrides pulled from Consul KV
- Auto-deregistration of stale Consul service instances after 30s
- GitHub Actions CI workflow for each service (user, product, order, inventory)
- Testcontainers-based integration tests for product-service, order-service, and inventory-service, with unit/integration Gradle tasks split out

## Initial platform scaffold (2025-06 – 2025-10)

### Added
- Order, Product, and User services scaffolded as independent Spring Boot apps
- CQRS split on order-service: PostgreSQL for commands, MongoDB for queries
- Kafka wired in for order-created events
- Service discovery via Consul, dynamic routing via Traefik
- Observability stack: Prometheus + Micrometer, Elasticsearch/Kibana/Fluent Bit for log aggregation
- Dockerfiles and docker-compose (dev/test) for local and CI environments
- API and database documentation per service, plus architecture diagram

### Fixed
- SQL init scripts for user-service and order-service databases
- Actuator health check failures caused by missing `prefer-ip-address` and incorrect `exclude`/`include` config
- Traefik path-prefix routing for product-service
