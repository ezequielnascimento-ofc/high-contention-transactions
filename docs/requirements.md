# Requirements

## 1. Purpose

This document defines the functional and non-functional requirements for High-Contention Transactions.

The requirements establish what the system must guarantee independently of the implementation strategy.

Implementation decisions must not weaken these requirements.

### Status Legend

- ✅ Implemented & Verified - built and covered by automated tests.
- 🔶 Partially Implemented - some coverage exists, but not fully verified or complete.
- ⬜ Not Yet Implemented - planned, no implementation yet.
- ➖ Not Applicable Yet - requirement depends on a layer that doesn't exist yet (e.g., no HTTP API).

## 2. Functional Requirements

### FR-001 - Resource Creation ✅

The system allows the creation of a resource with a finite available quantity.

Implemented via `CreateProductService` and `CreateInventoryService`.

### FR-002 - Resource Availability ✅

The system exposes the current available quantity of a resource.

Implemented via `GetInventoryService`.

### FR-003 - Resource Allocation ✅

The system allows a consumer to attempt to allocate one or more units from an available resource.

Implemented via `DecreaseStockService`.

### FR-004 - Allocation Success ✅

An allocation is considered successful only when the requested quantity has been committed via an atomic conditional `UPDATE` (`decreaseQuantity`), verified by affected-row count.

### FR-005 - Insufficient Availability ✅

An allocation is rejected when the requested quantity is greater than the currently available quantity, raising `InsufficientStockException`. Verified in `DecreaseStockConcurrencyTest`.

### FR-006 - Concurrent Allocation ✅

The system supports multiple concurrent allocation attempts targeting the same resource. Verified with 10, 100, 1,000, and 10,000 concurrent threads in `DecreaseStockConcurrencyTest`.

### FR-007 - Overselling Prevention ✅

The system never successfully allocates more units than were available. This is the central claim of the project and is empirically verified: across all tested thread counts, the final inventory quantity is exactly zero, never negative.

### FR-008 - Idempotency ⬜

Not yet implemented. No idempotency-key mechanism exists at the service or API level. Deferred until the REST API layer is built (see `problem.md`, Scope).

### FR-009 - Transactional Failure 🔶

`@Transactional` boundaries exist on all mutating service methods, ensuring an exception rolls back any partial write. Not yet stress-tested for mid-transaction infrastructure failures (e.g., connection drop between the check and the write).

### FR-010 - Multiple Application Instances ⬜

Not yet tested. The atomic `UPDATE` strategy is expected to hold correctness across multiple application instances, since the guarantee is enforced by the database, not by application-level state - but this has not been empirically verified with more than one running instance.

## 3. Consistency Requirements

### CR-001 - Non-Negative Availability ✅

Available quantity never becomes negative. Enforced both at the SQL level (`WHERE quantity >= :quantity`) and at the domain level (`Inventory.decrease()`). Verified by `DecreaseStockConcurrencyTest` final-state assertions.

### CR-002 - Resource Conservation ✅

```text
initial_quantity = final_quantity + successfully_allocated_quantity
```

Verified directly: in every concurrency test run, `successCount == initialStock` and `finalInventory.quantity() == 0`.

### CR-003 - Atomic Allocation ✅

An allocation either fully succeeds or consumes no quantity - no partial allocation. Guaranteed by the single-statement conditional `UPDATE`; there is no multi-step write path in the decrement flow.

### CR-004 - Committed State 🔶

An allocation is not reported successful unless the database reports the row as affected. Not yet tested against edge cases such as a connection failure between commit and response delivery to the client.

### CR-005 - Concurrent Correctness ✅

Holds regardless of the number of concurrent allocation attempts, verified from 10 up to 10,000 concurrent threads.

## 4. Performance Requirements ⬜

Not yet implemented. Current tests validate **correctness** under concurrency, not throughput or latency.

Performance targets will be defined after establishing the initial workload model. The system must eventually provide measurable results for:

- Throughput
- Successful allocations per second
- Rejected allocations per second
- p50 latency
- p95 latency
- p99 latency
- Error rate
- Database utilization
- Connection pool utilization
- CPU utilization
- Memory utilization

Performance claims must be supported by reproducible measurements - none have been made yet.

## 5. Reliability Requirements

### RR-001 - Failure Safety 🔶

`@Transactional` prevents intentional inconsistent commits on exception. Not yet stress-tested against unintentional/infrastructure-level failures.

### RR-002 - Database Failure ⬜

Behavior when the database becomes unavailable is not yet defined or tested.

### RR-003 - Dependency Failure ➖

No optional external dependencies exist yet (no cache, no message broker, no third-party API).

### RR-004 - Timeout Handling ⬜

Not yet implemented. No explicit handling of ambiguous outcomes on client-side timeout.

### RR-005 - Retry Safety ⬜

Not yet implemented. No deduplication mechanism exists; a client-side retry after a network timeout could currently cause a duplicate decrement. Related to FR-008 (Idempotency).

## 6. Scalability Requirements

The system should support horizontal scaling of application instances without relying on process-local state for correctness.

Correctness must not depend on:

- A single application instance
- JVM-local locks
- In-memory inventory state
- Thread synchronization within a single process

**Status: 🔶 Architecturally aligned, not yet empirically verified.** The atomic `UPDATE` strategy inherently avoids JVM-local locks and in-memory state - the current test suite (`DecreaseStockConcurrencyTest`) already exercises this indirectly by using real database transactions rather than in-memory synchronization. However, no test has been run with multiple concurrent application instances against the same database.

## 7. Observability Requirements ⬜

Not yet implemented beyond Spring Boot Actuator's default health endpoint. No structured logging, metrics, or tracing exists for allocation throughput, latency, contention, or connection pool saturation.

## 8. Security Requirements

The system must follow secure-by-default principles.

### SR-001 - Secrets 🔶

No secrets are currently committed to the repository (test configuration uses a local in-memory H2 database with placeholder credentials). Not yet audited for production configuration.

### SR-002 - Configuration 🔶

Test configuration is externalized via `application-test.yaml`. Production-specific externalized configuration (e.g., environment variables for PostgreSQL credentials) is not yet defined.

### SR-003 - Input Validation 🔶

Domain-level validation exists (`Product`, `Inventory` reject invalid state via their own invariants). Request-level input validation does not exist yet, since no HTTP layer/DTOs have been built.

### SR-004 - Resource Limits ⬜

Not yet defined. No rate limiting or input-size constraints exist.

### SR-005 - Least Privilege ⬜

Not yet evaluated - no production database user/permission model has been defined.

### SR-006 - Dependency Security ⬜

Not yet automated (no dependency vulnerability scanning configured in the build).

### SR-007 - Logging ➖

No structured logging exists yet beyond Spring Boot defaults, so there is nothing to audit for leakage.

### SR-008 - Container Security ⬜

No container image has been built yet.

### SR-009 - Error Handling ⬜

Not yet implemented. Without a `GlobalExceptionHandler`, unhandled exceptions would currently expose default Spring error responses. This is a known, tracked gap - planned as the next implementation step.

## 9. Testability Requirements 🔶

- Unit tests: ✅ Implemented (domain + application layers, `Product` and `Inventory` modules).
- Integration tests: ✅ Implemented (application services against a real H2 database via Spring context).
- Concurrency tests: ✅ Implemented (`DecreaseStockConcurrencyTest`, 10–10,000 threads).
- Load tests: ⬜ Not implemented.
- Stress tests: ⬜ Not implemented.
- Failure tests: ⬜ Not implemented (no simulated database/network failure scenarios).
- Idempotency tests: ⬜ Not implemented (depends on FR-008).

Correctness has been validated under concurrent execution, not only sequential tests - this is the strongest-covered requirement in the project so far.

## 10. Reproducibility Requirements 🔶

Each concurrency experiment (`DecreaseStockConcurrencyTest`) documents:

- ✅ Initial resource quantity (`threadCount / 2`)
- ✅ Number of requests / concurrency level (10, 100, 1,000, 10,000 threads)
- ✅ Test tool (JUnit 5 `@ParameterizedTest` + `ExecutorService` + `CountDownLatch`)

Not yet documented per experiment:

- ⬜ Hardware
- ⬜ JVM version
- ⬜ Application version
- ⬜ Database version (currently H2 in tests; PostgreSQL behavior not yet separately validated)
- ⬜ Connection pool configuration per run (fixed at 50 for all runs, not varied)
- ⬜ Test duration measurements

Results do not yet distinguish between baseline, experimental, and production-like measurements - only correctness has been established so far, not performance.

## 11. Engineering Constraints

- ✅ Java is the primary programming language.
- ✅ Simplest correct solution preferred - atomic conditional `UPDATE` was chosen over pessimistic/optimistic locking specifically for this reason (see `problem.md`).
- 🔶 PostgreSQL evaluated as the primary transactional datastore - configured as the production driver, but all current concurrency tests run against H2, not PostgreSQL. PostgreSQL-specific behavior is not yet separately validated.
- ✅ Redis not introduced without a demonstrated technical requirement - the atomic `UPDATE` strategy resolved correctness without needing external coordination.
- ✅ Distributed architecture not introduced without a demonstrated requirement.
- ⬜ Performance optimization supported by measurements - no performance work has started yet.
- ✅ Concurrency mechanisms have explicit correctness reasoning - documented in `invariants.md` and `problem.md`.
- 🔶 Security controls applied from the beginning - domain-level validation exists; request-level and infrastructure-level controls are pending (see Section 8).
- ✅ Architectural decisions with significant trade-offs documented - see `problem.md`, "Strategy adopted".

## 12. Requirement Validation

Every critical requirement must eventually have a corresponding verification mechanism.

The expected relationship is:

```text
Requirement
    ↓
Invariant / Expected Behavior
    ↓
Automated Test
    ↓
Experiment
    ↓
Evidence
```

**Current state**: the chain above is fully closed for the core correctness requirements (FR-004 through FR-007, CR-001 through CR-003, CR-005) - each has a corresponding invariant, an automated test, and reproducible evidence via `DecreaseStockConcurrencyTest`. The chain remains open for performance, reliability under failure, observability, and most security requirements, which are explicitly tracked as gaps in this document rather than assumed complete.
