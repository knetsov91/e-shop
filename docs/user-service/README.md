# User Service

## Overview

Serves as the Authorization Server for the platform. Handles user registration issues JWT access tokens and publishes the public key set used by other services to verify tokens locally. Built with Spring Authorization Server and follows the OAuth2 protocol. Registers itself with Consul so Traefik can route traffic to it dynamically.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Security OAuth2 Authorization Server
- PostgreSQL
- Spring Cloud Consul

## Endpoints

- `POST /api/v1/users/register` (Public) — Register a new user account
- `POST /oauth2/token` (Public) — Issue a JWT access token
- `GET /oauth2/jwks` (Public) — Public key set for JWT verification

## Authentication

Supports two OAuth2 clients:

- **eshop-service-client** — `client_credentials` grant used for service-to-service calls between order and product services
- **eshop-web-client** — `authorization_code` + PKCE grant used by the SPA frontend on behalf of a logged-in user

Tokens are signed with an RSA key pair generated at startup. Resource servers fetch the public key from `/oauth2/jwks` once and validate tokens locally on every request without contacting this service again.

## Configuration

The following environment variables are required to run the service:

- `USER_SERVICE_DB_HOST` — PostgreSQL host
- `USER_SERVICE_DB_PORT` — PostgreSQL port
- `USER_SERVICE_DB` — Database name
- `USER_SERVICE_DB_USERNAME` — Database username
- `USER_SERVICE_DB_PASSWORD` — Database password
- `ESHOP_SERVICE_CLIENT_SECRET` — Secret for the service-to-service OAuth2 client
- `CONSUL_URL` — Consul host for service registration and discovery
