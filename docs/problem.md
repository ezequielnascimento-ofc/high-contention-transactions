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

The project demonstrates the relationship between:

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

Architectural decisions are supported by reproducible tests and measurable evidence rather than assumptions.

**Strategy adopted**: atomic conditional `UPDATE` (`UPDATE inventory SET quantity = quantity - :qty WHERE id = :id AND quantity >= :qty`), chosen over pessimistic locking (`SELECT FOR UPDATE`) and optimistic locking (version column + retry). This strategy delegates the atomicity guarantee to the database itself — the availability check and the write happen in a single, indivisible operation, with no explicit application-level lock and no retry logic required. See `invariants.md` for the correctness argument and `workload.md` for the empirical validation.

## Scope

### In Scope

- Concurrent resource allocation.
- Transactional consistency.
- Concurrency control.
- Inventory management.
- Overselling prevention.
- PostgreSQL transaction behavior.
- Concurrency testing.
- Secure-by-default development
