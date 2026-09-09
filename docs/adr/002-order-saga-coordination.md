# ADR-002: Order Saga Coordination Strategy

**Status:** Accepted
**Date:** 2026-08-12
**Context:** Order, payment, and inventory services — cross-service order fulfillment flow

---

## Context

Today the order flow spans two services and is coordinated by choreography: **order-service** persists the order and publishes an `order-events` message via the transactional outbox (see ADR-001), **inventory-service** consumes it, attempts to reserve stock, and publishes `inventory-events`. **order-service** consumes that reply and flips the order's `status` field to `CONFIRMED` or `FAILED`. Neither service holds a model of the overall flow — each just reacts to the message in front of it.

**payment-service** is being added as a third participant, sitting between order creation and inventory reservation: an order needs to be paid before stock is reserved, and if reservation fails after payment succeeded, the payment needs to be refunded.

Two approaches were considered for coordinating the three-service flow.

---

## Options Considered

### Option A — Extend choreography

Add `payment-events` to the existing chain: order-service publishes `order-events`, payment-service consumes it, charges via Stripe, and publishes `payment-events`; inventory-service consumes a successful payment event and reserves stock; order-service consumes both `payment-events` and `inventory-events` to advance `status`. A failed reservation after a successful charge triggers a refund by having inventory-service publish a compensation event that payment-service consumes.

**Pros:**
- Consistent with the pattern already in place between order-service and inventory-service — no new coordination component.
- Services stay decoupled — none needs to know about the others' existence, only the topics they consume and produce.

**Cons:**
- The order's overall state is now implicit, split across three services' independent reactions to three topics. There's no single place that shows what step an order is on.
- Compensation (refunding a payment because reservation failed downstream) requires inventory-service to know it needs to trigger a refund — a service reasoning about a step it didn't perform, to undo a step it doesn't own.
- Debugging a stuck order means reconstructing the sequence from logs across three services and however many topics are involved, rather than reading a state field.
- Every new step (shipping, notifications, etc.) adds another set of implicit event contracts and another possible partial-failure path with no central view.

### Option B — Orchestration

order-service owns the saga. On order creation it writes `PENDING` and emits a `charge-requested` event via the outbox. It consumes the payment result: on success, it emits `reserve-stock-requested`; on decline, it marks the order `FAILED` and stops. It then consumes the inventory result: on `RESERVED`, it marks the order `CONFIRMED`; on `INSUFFICIENT`, it emits `refund-requested` (compensation) and marks the order `FAILED` once the refund confirms.

**Pros:**
- One place — order-service — holds the saga state and decides what happens next. Reading its state tells you exactly where an order is stuck and why.
- Compensation logic lives with the orchestrator, not scattered into services that only know their own step. payment-service and inventory-service just execute a command and report the outcome; they never decide to compensate on their own.
- Adding a step (e.g. shipping) means adding a state and a transition in one service, not a new implicit contract between two others.
- Reuses the outbox/relay infrastructure from ADR-001 for each step trigger, so the reliability guarantees already built for `order-events` extend naturally to `charge-requested` and `reserve-stock-requested`.

**Cons:**
- order-service becomes a hub that both payment-service and inventory-service are indirectly coupled to — a bug in the orchestrator can stall the whole flow.
- More upfront code: the orchestrator needs an explicit state machine rather than a single free-text `status` column, plus handling for each step's success/failure/timeout.
- payment-service and inventory-service still need to be idempotent consumers (already true today per ADR-001), since the orchestrator retries on timeout.

---

## Decision

**Orchestration (Option B).**

Choreography works when a flow has one hop and one failure mode, which is why it was sufficient for order → inventory alone. A three-step flow with a compensating transaction (refund on failed reservation) is exactly the case choreography handles worst: the compensation decision has to live somewhere, and scattering it into the service that happens to detect the failure produces implicit, hard-to-trace coupling instead of the decoupling choreography is meant to buy. Centralizing that decision in an orchestrator makes the failure and compensation paths explicit and puts the whole saga's state in one queryable place.

order-service is the natural owner — it already originates the order and owns the outbox used to publish the first event.

---

## Consequences

- `OrderCommand.status` (currently a free-text `String`) needs to become an explicit saga state — the two-value implicit transition (`PENDING` → `CONFIRMED`/`FAILED`) doesn't cover a multi-step flow with an in-flight charge or a pending refund.
- payment-service and inventory-service become command-and-report participants: they consume a request event, act, and publish a result. They carry no saga awareness, no knowledge of what happens after their result is consumed.
- The orchestrator must handle timeouts, not just success/failure replies — if payment-service never responds, the saga can't stay `PENDING` forever.
- Every new step trigger (`charge-requested`, `reserve-stock-requested`, `refund-requested`) goes through the same outbox/relay path as `order-events`, so at-least-once delivery and idempotent consumers (ADR-001) remain the baseline guarantee.
- This adds a hub-and-spoke dependency shape: order-service now needs to know both payment-service's and inventory-service's request/reply contracts. That coupling is the deliberate tradeoff for having one place to read the saga's state.
