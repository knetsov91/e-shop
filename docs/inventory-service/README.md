# Inventory Service

## Overview

Manages product stock levels. Listens for order events on Kafka and decrements stock when an order is placed. Registers with Consul for service discovery.

## Tech Stack

- Java 21
- Spring Boot 3
- PostgreSQL
- Apache Kafka
- Spring Cloud Consul

## Configuration

The following environment variables are required to run the service:

- `INVENTORY_SERVICE_DB_HOST` — PostgreSQL host
- `INVENTORY_SERVICE_DB_PORT` — PostgreSQL port
- `INVENTORY_SERVICE_DB` — PostgreSQL database name
- `INVENTORY_SERVICE_DB_USERNAME` — PostgreSQL username
- `INVENTORY_SERVICE_DB_PASSWORD` — PostgreSQL password
- `CONSUL_URL` — Consul host for service registration and discovery

Non-secret settings — server port, JPA/Hibernate config, Kafka consumer/producer settings, Sentry log level — are not set via environment variables but loaded from Consul KV at startup (see the root README's [Centralized configuration via Consul KV](../../README.md#design-decisions) section).
