# Changelog

No tagged releases yet, so this is organized by development phase instead of version number. Newest first.

## Custom Sentry spans for order creation and payment processing (2026-09)

### Added
- Child spans on order creation (`persist order command`, `write order query projection`, `write payment-requested outbox event`), attached to the transaction Sentry's servlet filter already starts for the HTTP request
- An explicit `payment.process` transaction in payment-service's `PaymentRequestConsumer`, since Kafka listeners get no transaction of their own — with child spans around persisting the payment record and publishing the result back to Kafka
- Sentry Gradle plugin and `sentry.dsn` config added to payment-service, which had none at all until now

## Order ownership, query API, and admin bootstrap (2026-09)

### Added
- `GET /api/v1/orders/{orderId}` single-order lookup, a `status` filter, and pagination on `GET /api/v1/orders`
- `userId` on `OrderCommand` and `OrderQuery`, captured from the JWT subject at order creation — the prerequisite for order ownership
- A `role` claim embedded in the JWT issued by user-service
- Ownership-based filtering on `GET /api/v1/orders`: regular users only see their own orders, admins see all of them
- `AdminBootstrapRunner` in user-service — creates the first admin account from `ADMIN_USERNAME`/`ADMIN_EMAIL`/`ADMIN_PASSWORD` on startup if none exists yet, since public registration always assigns `ROLE_USER` by design and there was previously no way to obtain an admin account at all

### Fixed
- **Bug**: `GET /api/v1/orders` existed, was tested, and looked complete, but every write path (order creation, `PaymentEventConsumer`, `InventoryEventConsumer`) only ever touched `OrderCommand` (PostgreSQL) — nothing wrote to `OrderQueryRepository` (MongoDB) at all, so the endpoint always returned empty against real data. All three now also write/update the `OrderQuery` projection.
- **IDOR**: before ownership filtering, any authenticated user could read any other user's orders through `GET /api/v1/orders` — the endpoint had no concept of who was asking.
- `OrderQuery`'s `@Id` was `jakarta.persistence.Id`, which Spring Data MongoDB doesn't recognize — `orderId` was never actually mapped to the document's `_id`, so `findById(orderId)` could never have matched anything, now fixed to `org.springframework.data.annotation.Id`

## BFF pattern for SPA token handling (2026-09)

### Added
- Backend-for-frontend token handling in user-service for the SPA client — the authorization code + PKCE exchange happens server-side, and the access token is kept out of browser JS via an HttpOnly cookie instead of being returned directly to the SPA

## Swagger/OpenAPI documentation restored (2026-09)

### Added
- `springdoc-openapi` dependency restored on order-service, product-service, and user-service (had been reverted at some point — no OpenAPI docs were being served at all)
- A global bearer-JWT security scheme registered on each service's `OpenApiConfig`, so Swagger UI shows an Authorize button and protected endpoints can actually be tried out from the UI instead of failing with an unfixable 401
- Swagger UI and OpenAPI docs paths permitted through Spring Security on order-service and user-service

### Fixed
- product-service was missing JWT authentication entirely on some endpoints
- product-service was missing the `spring-cloud-starter-consul-config` dependency, so it couldn't pull its own config from Consul KV

## Testcontainers-based integration testing (2026-07 – 2026-08)

### Added
- Testcontainers dependencies and shared container config (Postgres/MongoDB/Kafka as applicable) added to product-service, order-service, and inventory-service
- Unit and integration test Gradle tasks split out per service, so CI can run fast unit tests separately from slower container-backed ones
- Integration tests against real infrastructure: `OrderEventConsumerIT` (inventory-service), outbox relay publishing, `InventoryEventConsumer`, and `OrderCommandService.createOrder` (order-service), `ProductServiceIT` (product-service)

## Payment service & saga completion (2026-08)

### Added
- `eshop-payment-service` module (Stripe SDK, dev profile, Consul registration)
- `PaymentRequestedEvent` / `PaymentEvent` and Kafka producer/consumer wiring between order-service and payment-service
- `PaymentRequestConsumer` in payment-service, with retry and dead-letter topic handling
- `PaymentEventConsumer` in order-service to move orders through the saga on payment success/failure
- `OrderStatus` enum to model saga states explicitly instead of ad-hoc strings
- ADR-002 documenting the order saga coordination strategy

### Fixed
- Order creation flow updated to publish a payment request instead of an order-created event, closing a gap where orders could be marked complete before payment was confirmed

## Centralized configuration & CI (2026-06 – 2026-07)

### Added
- Consul KV used as the shared config source for Kafka, Sentry, and management endpoint settings across all services, replacing duplicated per-service YAML
- Per-service server port, JPA, and Kafka overrides pulled from Consul KV
- Auto-deregistration of stale Consul service instances after 30s
- GitHub Actions CI workflow for each service (user, product, order, inventory)

## Observability: Sentry (2026-06)

### Added
- Sentry Gradle plugin (auto-instrumentation) added to user-service, product-service, order-service, and inventory-service
- Error and log monitoring configured per service, including capture of key domain events (order placed/updated, product created, stock reserved/insufficient) and DLT messages

## Transactional outbox pattern (2026-06)

### Added
- `OutboxEvent`, `OutboxEventRepository`, and a scheduled `OutboxEventRelay` in order-service — replaces direct Kafka publish on order creation with a transactional outbox, so an order write and its event only ever go out together

## Inventory service & idempotent event handling (2026-05 – 2026-07)

### Added
- `eshop-inventory-service` with `Stock` entity (optimistic locking) and `InventoryService.reserveStock`
- `OrderEventConsumer` to consume `order-events` from Kafka, with retry + dead-letter topic
- `InventoryEvent` publishing back to order-service after stock reservation
- `ProcessedOrderEvent` table to make `reserveStock` idempotent against duplicate Kafka deliveries — skips re-publishing `inventory-events` for messages already processed

### Fixed
- Duplicate `order-events` deliveries no longer double-decrement stock (root cause of a bug found via a real-infrastructure Testcontainers test simulating redelivery)

## Traefik rate limiting & load testing (2026-05)

### Added
- Rate-limit middleware defined via Traefik's file provider and attached to every service router through Consul tags
- Gatling load-test subproject (`eshop-load-tests`) with a `RateLimitSimulation` to verify the limiter under load

## Auth & security (2026-05)

### Added
- User-service configured as an OAuth2 Authorization Server with RSA JWK and registered clients (service + web, OIDC support)
- Order-service configured as an OAuth2 resource server, validating JWTs against the JWK endpoint
- `UserService`/`UserRepository`/`RegisterRequest` — registration endpoint with bcrypt password encoding

### Fixed
- CSRF checks disabled on user-service (SameSite cookies already prevent cross-site requests)
- Service client secret moved from hardcoded value to environment variable
- OAuth2 endpoints registered in Consul tags so Traefik actually routes to them

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
