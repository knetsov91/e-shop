# E-Shop — Microservices Platform

A backend e-commerce platform built with a microservice architecture. Each service is independently deployable, communicates via Kafka for async events, and registers itself dynamically with Traefik through Consul — no static routing configuration required.

## Functional Requirements

### User Service
- Register a new user account
- Authenticate a user and issue a session token
- Retrieve user profile information

### Product Service
- List all available products
- Create a new product with name, description, price, and stock quantity
- Retrieve a single product by ID

### Order Service
- Place a new order for a product with a specified quantity
- Publish an order-created event so downstream services can react asynchronously
- Retrieve the list of all orders (query side, CQRS)

## Non-Functional Requirements

### Observability
- Prometheus metrics exposed on every service via `/actuator/prometheus`
- Centralised log aggregation with Fluent Bit, Elasticsearch, and Kibana
- Grafana dashboards for real-time service health and throughput monitoring

## Architecture

<img src="./assets/e-shop-diagram.png" />

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Cloud 2025
- PostgreSQL
- MongoDB
- Apache Kafka
- HashiCorp Consul
- Traefik
- Prometheus
- Grafana
- Elasticsearch
- Kibana
- Fluent Bit
- Docker

## Usage

**Prerequisites:** Docker and Docker Compose

Create an `.env` file inside `infrastructure/` with the required variables (see each service's docs for details), then run:

```bash
cd infrastructure

# Development — debug logging, no observability
docker compose -f docker-compose-dev.yaml up

# Production — full stack with observability
docker compose up
```

## Design Decisions

**CQRS in the order service.** Orders are written to PostgreSQL and simultaneously projected into MongoDB as a read model. The write side owns consistency; the read side is optimised for queries without joins or locking contention. This makes the query path independently scalable and keeps the write path simple.

**Kafka for async communication.** When an order is placed the service publishes an event instead of calling downstream services directly. This keeps services decoupled — a slow or unavailable consumer does not block the order write path, and new consumers can be added without changing the order service.

**Self-hosted OAuth2 Authorization Server.** The user service runs Spring Authorization Server and issues signed JWT access tokens. Other services validate tokens locally using the public key fetched once from the JWK endpoint — no per-request round-trip to the auth server. Two OAuth2 clients are registered: `client_credentials` for service-to-service calls and `authorization_code + PKCE` for the SPA frontend.

**Consul for service discovery.** Each service registers itself in Consul at startup and exposes a health-check endpoint. Consul acts as the central registry, so no service needs to know the address of another at deploy time — they resolve each other by name through the registry.

**Centralized configuration via Consul KV.** Non-secret config lives in Consul's key-value store instead of each service's local yaml configuration file. Every service loads two keys at startup: a shared **config/application** key common to all services (Kafka bootstrap address, management endpoint exposure, Sentry tracing defaults) and its own **config/{service-name}** key for values specific to that service (server port, JPA/Hibernate settings, Kafka consumer/producer settings, Sentry log level). This means common settings are defined once instead of duplicated across services and any of these values can be changed and picked up without a rebuild. Secrets and connection bootstrap info (DB credentials, the Consul address itself) stay in environment variables, since a service needs those to locate Consul before it can pull anything from it.

**Traefik as the API gateway.** Traefik watches Consul and builds its routing table dynamically from the tags each service registers. It handles path-based routing, load balancing across instances, and TLS termination without any static configuration. Adding or removing a service instance requires no change to the gateway.

**Rate limiting at the gateway.** Each service route has a token bucket rate limiter defined in Traefik's dynamic config. The bucket refills at a fixed rate and allows short bursts above it — once the bucket is empty, requests get a 429 until it refills. The limit is per client IP and per route, so hitting `/api/v1/products` doesn't affect the budget for `/api/v1/orders`. Note: Traefik has no JWT awareness, so limiting per authenticated user isn't possible at this layer. That would require either implementing it inside each service or using a Redis-backed shared counter keyed by the JWT `sub` claim, which keeps the logic centralised and consistent across all instances.

## Troubleshooting

**Testcontainers can't connect to Docker: "client version 1.32 is too old."**
Problem: Spring Boot 3.5.0's managed Testcontainers version (`1.21.0`) bundles a Docker client that defaults to API version `1.32` during its initial connectivity check, which newer Docker Engine releases (API `1.40`+) reject outright. Setting `DOCKER_API_VERSION` doesn't help — that env var doesn't reach whatever internal client Testcontainers uses for this specific check.
Solution: Pin a newer Testcontainers release directly via `testcontainers-bom` in `dependencyManagement`, overriding Spring Boot's managed version, rather than relying on the BOM default.

**Testcontainers module dependency won't resolve on 2.x.**
Problem: `org.testcontainers:kafka`, `org.testcontainers:postgresql`, and `org.testcontainers:junit-jupiter` (the 1.x artifact names) don't exist under those artifact IDs in 2.x.
Solution: Use the renamed 2.x artifacts — `org.testcontainers:testcontainers-kafka`, `org.testcontainers:testcontainers-postgresql`, `org.testcontainers:testcontainers-junit-jupiter`.

**`KafkaContainer` silently builds a malformed image name on Testcontainers 2.x.**
Problem: The legacy `org.testcontainers.containers.KafkaContainer` class still exists in 2.x for backward compatibility, but its single-`String` constructor keeps 1.x's deprecated behavior — treating the argument as just a version tag appended to a hardcoded `confluentinc/cp-kafka` base image, so passing a full image reference (e.g. `apache/kafka-native:3.8.0`) silently produces a malformed image name. Guides and examples written for Testcontainers 1.x correctly use the `org.testcontainers.containers` package instead — that package only became the deprecated legacy path once 2.x introduced the split, so version context matters when following an example.
Solution: Use the new `org.testcontainers.kafka.KafkaContainer` class instead.

**`spring-boot-testcontainers` binary compatibility risk with Testcontainers 2.x.**
Problem: `spring-boot-testcontainers` is built against Testcontainers 1.x. Mixing it with Testcontainers 2.x artifacts risks binary incompatibility.
Solution: If a test only needs manual container wiring via `@DynamicPropertySource` (not `@ServiceConnection`), skip this dependency entirely and sidestep the version conflict.

**`KafkaTestUtils.consumerProps` silently misconfigures consumer properties.**
Problem: The signature is `consumerProps(String brokers, String group, String autoCommit)` — brokers first. Swapping the order silently produces a broken config (e.g. a broker address ending up in the `enable.auto.commit` property) rather than a compile error.
Solution: Pass arguments in the correct order — brokers, then group, then autoCommit.

## Documentations

- User microservice — [docs](./docs/user-service/README.md)
- Order microservice — [docs](./docs/order-service/README.md)
- Product microservice — [docs](./docs/product-service/README.md)
