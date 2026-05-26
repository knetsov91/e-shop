# Order Service

## Overview

Handles order creation and retrieval. Implements the CQRS pattern with PostgreSQL as the write store and MongoDB as the read store. On every new order a Kafka event is published so downstream services can react asynchronously. Registers itself with Consul so Traefik can route traffic to it dynamically.

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
