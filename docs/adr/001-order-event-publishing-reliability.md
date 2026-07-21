# ADR-001: Order Event Publishing Reliability

**Status:** Accepted  
**Date:** 2026-06-08  
**Context:** Order service — **OrderCommandService.createOrder**

---

## Context

When an order is created, two things must happen: the order is persisted to Postgres, and an **order-events** Kafka message is published so the inventory service can reserve stock. These are two separate I/O operations with no shared transaction boundary.

The current implementation calls **kafkaTemplate.send()** and discards the returned **CompletableFuture**. If the broker is unreachable, rejects the message, or the JVM crashes after the DB write, the order is committed as **PENDING** with no event ever published. It stays stuck permanently — there is no recovery path.

Two approaches were considered to fix this.

---

## Options Considered

### Option A — Async callback with compensation

Attach a **whenComplete** callback to the send future. If publishing fails, open a new transaction and flip the order status to **FAILED**.

**Pros:**
- Minimal code change — no new tables or infrastructure
- Non-blocking — HTTP request returns as soon as the DB write completes

**Cons:**
- No atomicity. The DB commit happens before the Kafka result is known. Between the commit and the callback firing, the system is in an inconsistent state.
- JVM crash in that window leaves the order stuck as **PENDING** with no mechanism to detect or recover it.
- Compensation is best-effort — if the callback itself fails, the order never gets updated.
- **FAILED** is the wrong terminal state for "we couldn't publish the event." It conflates a transient infrastructure failure with a genuine business failure.

### Option B — Transactional Outbox

Write the event as a row in an **outbox** table inside the same DB transaction as the order save. A separate poller reads unpublished rows and publishes them to Kafka, then marks them as published.

**Pros:**
- The order save and the outbox write are atomic. If the DB transaction commits, the event is guaranteed to exist. If it rolls back, neither is persisted.
- Survives JVM crashes — unpublished rows persist in the DB and will be picked up on restart.
- Kafka availability is fully decoupled from the HTTP write path. An outage delays event delivery but does not affect order creation.
- Unpublished rows are directly queryable — operational visibility with a simple **SELECT**.

**Cons:**
- Additional moving parts: a new **outbox** table, a poller process, and published/failed state tracking.
- Introduces publishing lag proportional to the poller interval.
- The poller can publish a message and then crash before marking the row as published, so consumers must handle duplicates — which is already required given Kafka's at-least-once delivery guarantee.

---

## Decision

**Transactional Outbox (Option B).**

Option A fixes the visibility problem — failures are no longer silent — but it does not fix the reliability problem. There is still a window where the order is committed and the event is lost, and no mechanism to recover from it automatically. Compensation works when Kafka is temporarily unavailable and recovers quickly, but it provides no guarantee for cases where the failure is persistent or the JVM dies mid-flight.

The outbox pattern eliminates the window entirely. Once the DB transaction commits, the event exists durably and will be delivered regardless of what happens next. This is the only approach that gives a genuine at-least-once delivery guarantee on the publishing side without blocking the HTTP thread on broker availability.

The operational overhead is real but bounded: one new table, one poller (a lightweight scheduled task or Debezium), and a **published** flag. Given the system already depends on Kafka for order correctness, the reliability guarantee is worth the added complexity.

---

## Consequences

- The **order-events** publish lag is no longer zero — it is bounded by the poller interval. For most use cases a sub-second interval is sufficient.
- Consumers of **order-events** must be idempotent (already required by at-least-once Kafka semantics, and already flagged as a known gap in the resiliency backlog).
- The **outbox** table acts as an audit log of all events the order service has ever attempted to publish, which is useful for debugging and replay.
- The relay uses **SELECT FOR UPDATE** to prevent concurrent instances from processing the same rows. **SKIP LOCKED** can be applied as an optimization — instead of waiting for a lock held by another instance, the poller skips locked rows and returns immediately, reducing contention under high polling frequency.
