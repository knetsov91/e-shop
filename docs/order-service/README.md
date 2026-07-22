# Order Service

## Overview

Handles order creation and retrieval. Implements the CQRS pattern with PostgreSQL as the write store and MongoDB as the read store. On every new order a Kafka event is published so downstream services can react asynchronously. Registers itself with Consul so Traefik can route traffic to it dynamically.

## Functional Requirements

- Place a new order for a product with a specified quantity
- Publish an order-created event so downstream services can react asynchronously
- Retrieve all orders from the query side

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

Non-secret settings — server port, JPA/Hibernate config, Kafka consumer group, MongoDB database name, Sentry log level — are pulled from Consul KV at startup instead (see the root README's [Centralized configuration via Consul KV](../../README.md#design-decisions) section).
