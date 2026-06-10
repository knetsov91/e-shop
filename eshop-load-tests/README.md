# Load Tests

Gatling-based simulations covering rate limiting verification, load testing, and performance testing. Each section groups simulations by what they test.

## Prerequisites

The full stack must be running before executing any simulation:

```bash
cd infrastructure
docker compose -f docker-compose-dev.yaml up
```

## Running

```bash
cd eshop-load-tests
./gradlew gatlingRun
```

Reports are written to `build/reports/gatling/` after each run.

## Rate Limiting

Traefik enforces rate limiting via a token bucket strategy. Each client is allocated a bucket of tokens that refills at a fixed rate; once the bucket is empty, further requests are rejected until tokens are refilled.

### RateLimitSimulation

Fires 500 concurrent users at `GET /api/v1/products` all at once and asserts:

- Throughput exceeds 100 req/s — the gateway is processing requests fast enough that failures are caused by the rate limiter, not by the gateway being too slow
- Some requests fail — the bucket is exhausted and excess traffic is being rejected
- Not all requests fail — tokens are still available for traffic within the allowed rate
