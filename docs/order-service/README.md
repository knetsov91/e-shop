# Order Service

## Overview

Handles order creation and retrieval. Implements the CQRS pattern with PostgreSQL as the write store and MongoDB as the read store. Owns the order saga: it triggers payment, then inventory reservation, reacting to each step's result instead of relying on services to choreograph off each other's events (see [ADR-002](../adr/002-order-saga-coordination.md)). Registers itself with Consul so Traefik can route traffic to it dynamically.

## Functional Requirements

- Place a new order for a product with a specified quantity and amount
- Drive the order through its saga: request payment, then trigger inventory reservation once payment succeeds
- Retrieve all orders from the query side

## Saga

Order status moves through `PENDING` → `AWAITING_INVENTORY` → `CONFIRMED`, or terminates early at `PAYMENT_FAILED` / `FAILED`. On order creation, order-service publishes to `payment-requests` and waits; it does not reserve stock until payment succeeds.

- `payment-requests` (published) — `PaymentRequestedEvent(orderId, amount, currency)`, consumed by payment-service
- `payment-events` (consumed) — `PaymentEvent(orderId, status)`; `SUCCEEDED` moves the order to `AWAITING_INVENTORY` and publishes `order-events` to trigger stock reservation, `FAILED` moves it to `PAYMENT_FAILED`
- `order-events` (published) — unchanged contract, now published on payment success rather than at order creation
- `inventory-events` (consumed) — unchanged, moves the order to `CONFIRMED` or `FAILED`

Currency is hardcoded to `USD` for now — order-service has no multi-currency support yet. Refund compensation on failed stock reservation (after payment already succeeded) is not implemented yet; it's a planned follow-up on top of this.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security OAuth2 Resource Server
- PostgreSQL
- MongoDB
- Apache Kafka
- Spring Cloud Consul

## Endpoints

- `POST /api/v1/orders` (Protected) — Place a new order and publish an event to Kafka
- `GET /api/v1/orders` (Protected) — Retrieve all orders from the query side

## Authentication

Validates incoming JWT access tokens locally using the public key fetched from the user service JWK endpoint. No request is made to the user service per request — the key is cached after the first fetch.

## Transactional Outbox

Order events are not published to Kafka directly. Instead, the event is written to an **outbox** table in the same PostgreSQL transaction as the order save. This guarantees that an event is never lost — if the transaction commits, the event exists durably regardless of Kafka availability.

A scheduled relay (**OutboxEventRelay**) polls the **outbox** table every 5 seconds for unpublished rows. For each event it sends the payload to the configured Kafka topic synchronously, then lets Hibernate mark the row as **published = true** on transaction commit. If the Kafka send fails, the row stays unpublished and is retried on the next poll.

The relay uses **SELECT FOR UPDATE** to lock unpublished rows during processing. This prevents multiple service instances from picking up the same event concurrently.

Consumers of **order-events** must be idempotent — the relay provides at-least-once delivery, meaning a message can be published more than once if the relay crashes after the Kafka send but before the transaction commits.

## Observability

Error tracking and logging is handled by Sentry. Unhandled exceptions and all WARN-level (and above) log entries are forwarded automatically. The following business events are logged explicitly:

- Order placed — logged after the order is persisted
- Order status updated to CONFIRMED or FAILED — logged after the inventory response is processed
- Dead-letter topic message — logged as ERROR when a Kafka message exhausts all retries

## Configuration

The following environment variables are required to run the service:

- `ORDER_SERVICE_DB_HOST` — PostgreSQL host
- `ORDER_SERVICE_DB_PORT` — PostgreSQL port
- `ORDER_SERVICE_DB` — PostgreSQL database name
- `ORDER_SERVICE_DB_USERNAME` — PostgreSQL username
- `ORDER_SERVICE_DB_PASSWORD` — PostgreSQL password
- `ORDER_SERVICE_QUERY_DB_HOST` — MongoDB host
- `ORDER_SERVICE_QUERY_DB_USERNAME` — MongoDB username
- `ORDER_SERVICE_QUERY_DB_PASSWORD` — MongoDB password
- `AUTH_SERVICE_JWK_URI` — JWK endpoint of the user service for JWT verification
- `CONSUL_URL` — Consul host for service registration and discovery
- `SENTRY_DSN` — Sentry DSN for error tracking and logging

Non-secret settings — server port, JPA/Hibernate config, Kafka consumer group, MongoDB database name, Sentry log level — are pulled from Consul KV at startup instead (see the root README's [Centralized configuration via Consul KV](../../README.md#design-decisions) section).
