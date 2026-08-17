# Problem Definition

## Context

Modern transactional systems may receive a large number of concurrent operations targeting the same limited resource.

When contention becomes high, seemingly correct application logic can produce race conditions, inconsistent state, lost updates, overselling, excessive lock contention, or degraded latency.

## Problem

The project investigates how to safely process concurrent transactions competing for a finite resource while preserving correctness and transactional consistency.

The primary scenario is inventory allocation, where a limited number of units can be acquired by a large number of concurrent requests.

The central challenge is not simply handling a high request volume.

The challenge is maintaining system invariants when many operations attempt to modify the same shared state at the same time.

## Engineering Goal

Design, implement, and evaluate transactional concurrency-control strategies capable of preventing overselling while maintaining measurable performance under high contention.

The project should demonstrate the relationship between:

- concurrency;
- transaction boundaries;
- atomicity;
- isolation;
- contention;
- consistency;
- throughput;
- latency;
- failure handling;
- scalability.

Architectural decisions must be supported by reproducible tests and measurable evidence rather than assumptions.

## Scope

### In Scope

- Concurrent resource allocation.
- Transactional consistency.
- Concurrency control.
- Inventory management.
- Overselling prevention.
- Idempotency.
- PostgreSQL transaction behavior.
- Redis evaluation where technically justified.
- Concurrency testing.
- Load testing.
- Performance benchmarking.
- Observability.
- Resilience.
- Secure-by-default development practices.

### Out of Scope

- Real payment processing.
- Frontend development.
- Shipping and fulfillment.
- Full product catalog management.
- Kubernetes.
- Microservices without a demonstrated requirement.
- Distributed messaging without a demonstrated requirement.
- Cloud infrastructure without a demonstrated requirement.

## Initial Scenario

The initial scenario consists of a finite inventory being accessed concurrently by a large number of purchase attempts.

Example workload:

- Initial inventory: 1,000 units.
- Purchase attempts: 10,000.
- High concurrent request volume.
- Multiple requests targeting the same inventory.

The exact workload model will be formally defined before performance testing begins.

## Expected Outcome

The system must prevent successful operations from consuming resources that are no longer available.

Correctness must be demonstrated through automated tests and reproducible experiments.

Performance claims must be supported by measurements.
