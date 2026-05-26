# Product Service

## Overview

Handles product listing and creation. Products are stored in MongoDB. Listing is publicly accessible while creating a product requires a valid JWT token. Registers itself with Consul so Traefik can route traffic to it dynamically.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security OAuth2 Resource Server
- MongoDB
- Spring Cloud Consul

## Endpoints

- `GET /api/v1/products` (Public) — List all products
- `POST /api/v1/products` (Protected) — Create a new product

## Authentication

Validates incoming JWT access tokens locally using the public key fetched from the user service JWK endpoint. No request is made to the user service per request — the key is cached after the first fetch.

## Configuration

The following environment variables are required to run the service:

- `ORDER_SERVICE_QUERY_DB_HOST` — MongoDB host
- `ORDER_SERVICE_QUERY_DB_USERNAME` — MongoDB username
- `ORDER_SERVICE_QUERY_DB_PASSWORD` — MongoDB password
- `AUTH_SERVICE_JWK_URI` — JWK endpoint of the user service for JWT verification
- `CONSUL_URL` — Consul host for service registration and discovery
