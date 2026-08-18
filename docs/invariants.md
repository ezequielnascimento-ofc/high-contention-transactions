# System Invariants

## 1. Purpose

This document defines the invariants that must remain true throughout the execution of the system.

An invariant is a condition that must hold regardless of:

- Request volume;
- Concurrency level;
- Number of application instances;
- Transaction interleaving;
- Retries;
- Expected failures;
- Database contention.

Concurrency-control mechanisms must be evaluated based on their ability to preserve these invariants.

### Status Legend

- ✅ Proven - verified by an automated, reproducible test.
- 🔶 Architecturally Aligned - the design satisfies the invariant by construction, but no dedicated test proves it yet.
- ⬜ Not Yet Implemented - no implementation or verification exists.
- ➖ Not Applicable Yet - depends on a feature that doesn't exist yet (e.g., idempotency keys, multi-instance deployment).

## 2. Inventory Invariants

### INV-001 - Non-Negative Inventory ✅

Available inventory must never become negative.

```text
available_stock >= 0
```

**Proof**: enforced at the SQL level (`WHERE quantity >= :quantity` in `decreaseQuantity`) and verified empirically in `DecreaseStockConcurrencyTest` - final inventory is exactly `0` across all tested thread counts (10, 100, 1,000, 10,000), never negative.

---

### INV-002 - No Overselling ✅

The total quantity successfully allocated must never exceed the initial available quantity.

    successful_allocated_quantity <= initial_stock

For a single-unit allocation model:

    successful_allocations <= initial_stock

**Proof**: `DecreaseStockConcurrencyTest` asserts `successCount == initialStock` exactly, for every tested concurrency level. This is the central claim of the project and its strongest-evidenced invariant.

---

### INV-003 - Inventory Conservation ✅

For a resource whose only state transition is successful allocation:

    initial_stock = final_stock + successfully_allocated_quantity

Example (as executed in the test suite, `threadCount = 1000`):

    Initial stock:                500
    Successful allocations:       500
    Final stock:                    0

Therefore:

    500 = 500 + 0

**Proof**: verified directly in `DecreaseStockConcurrencyTest` for all four thread counts (10, 100, 1,000, 10,000).

---

### INV-004 - Atomic Allocation ✅

A successful allocation must consume the complete requested quantity.

An allocation must not result in a partially committed resource consumption unless partial allocation is explicitly supported by the domain model.

The operation must therefore have an atomic outcome:

    SUCCESS  → requested quantity consumed
    REJECTED → no quantity consumed
    FAILED   → no committed quantity consumed

**Proof**: guaranteed structurally - `decreaseQuantity` is a single conditional `UPDATE` statement; there is no multi-step write path, so partial consumption is not representable by the implementation, not just avoided by discipline.

## 3. Transaction Invariants

### INV-005 - Committed Success 🔶

A successful allocation must correspond to a committed state transition.

The system must not report:

    SUCCESS

when the corresponding resource mutation was rolled back or never committed.

**Status**: `@Transactional` on `DecreaseStockService.execute()` ensures the report of success only happens after the method returns without exception, which implies commit under normal Spring transaction semantics. Not yet tested against edge cases (e.g., a commit that fails silently at the JDBC driver level after the method returns).

---

### INV-006 - Rollback Preservation 🔶

If an allocation transaction is rolled back, the allocation must not permanently reduce available inventory.

Before:

    stock = N

After rollback:

    stock = N

**Status**: architecturally guaranteed by `@Transactional` (any exception inside `execute()` triggers rollback of the whole method). No dedicated test forces a mid-transaction failure to verify this explicitly.

---

### INV-007 - Isolation From Uncommitted State ⬜

A transaction must not make correctness decisions based on invalid or uncommitted state.

The implementation must explicitly define the transaction isolation behavior required to preserve the system invariants.

**Status**: no explicit isolation level has been configured or documented. The default isolation level of the underlying database/driver is currently relied upon implicitly. This needs to be made explicit before the invariant can be considered addressed.

## 4. Concurrency Invariants

### INV-008 - Concurrency Independence ✅

System correctness must not depend on the order in which concurrent requests are executed.

Given the same initial state and the same set of valid allocation requests, different transaction interleavings must not produce an invalid committed state.

**Proof**: `DecreaseStockConcurrencyTest` releases all threads simultaneously via `CountDownLatch`, meaning execution order/interleaving is effectively randomized by the JVM scheduler across runs - the invariant held across all tested runs regardless of interleaving.

---

### INV-009 - No Lost Allocation ✅

A successfully committed allocation must not be silently overwritten by another concurrent operation.

Every committed successful allocation must be reflected in the resulting resource state.

**Proof**: this is the classic _lost update_ problem the atomic `UPDATE` strategy was specifically chosen to eliminate. Verified indirectly by INV-002/INV-003 - if updates were being lost, `successCount` would not exactly equal `initialStock`.

---

### INV-010 - No Duplicate Allocation ⬜

The same logical allocation request must not consume the resource more than once when the system's idempotency contract requires duplicate requests to represent the same operation.

**Status**: not implemented. No request-level deduplication mechanism (idempotency key) exists yet. Tracked as a dependency of FR-008 in `requirements.md`.

## 5. Failure Invariants

### INV-011 - Application Failure Safety ⬜

An application failure must not create a committed inventory state that violates the inventory invariants.

**Status**: not yet tested. No fault-injection or failure-simulation test exists (e.g., forcing an exception between the `UPDATE` and the response).

---

### INV-012 - Database Failure Safety ⬜

Database failures must not result in a committed state that violates the inventory invariants.

The system must explicitly define the observable outcome when the result of a transaction cannot be determined by the client.

**Status**: not yet defined or tested. No behavior has been specified for database unavailability or ambiguous transaction outcomes.

---

### INV-013 - Retry Safety ⬜

Retrying an operation must not cause additional resource consumption beyond what is permitted by the operation's idempotency semantics.

**Status**: not implemented - depends on INV-010/FR-008.

## 6. Multi-Instance Invariants

### INV-014 - Process Independence ✅

Correctness must not depend on JVM-local synchronization mechanisms.

The following must not be required for correctness:

- `synchronized`
- JVM-local locks
- Local mutexes
- In-memory inventory state

Such mechanisms may be evaluated experimentally, but they cannot be the foundation of correctness when multiple application instances are supported.

**Proof**: verified by construction - `decreaseQuantity`/`increaseQuantity` use no `synchronized` block, no in-memory lock, and no JVM-local state. Correctness is delegated entirely to the database's row-level atomicity for the `UPDATE` statement. This is a direct, structural consequence of the strategy chosen in `problem.md`.

---

### INV-015 - Shared State Consistency 🔶

Multiple application instances operating concurrently against the same resource must preserve all system invariants.

Example:

    Instance A ─┐
    Instance B ─┼──> Shared Resource
    Instance C ─┘

The final state must remain valid regardless of which instance processes each request.

**Status**: architecturally expected to hold, since INV-014 already ensures no process-local state is involved - the current concurrency test only exercises multiple _threads_ within a single JVM/application instance, not multiple separate application instances against the same database. Not yet empirically verified with more than one instance.

## 7. Idempotency Invariants

### INV-016 - Idempotent Allocation ⬜

When an allocation request is identified as idempotent, processing the same logical request multiple times must produce the same effective resource consumption as processing it once.

For example:

    Request ID: A123

    First request → SUCCESS → consume 1 unit
    Retry         → SUCCESS/REPLAYED → consume 0 additional units

**Status**: not implemented. The exact API semantics are deferred until the REST API layer exists (see `requirements.md`, FR-008).

## 8. Observability Invariants

### INV-017 - Outcome Traceability ⬜

Every allocation attempt must produce an observable outcome that allows the system to distinguish, where applicable:

- Successful allocation
- Rejected allocation
- Validation failure
- Transaction failure
- Dependency failure
- Timeout
- Duplicate request

Observability must not compromise security or expose sensitive information.

**Status**: not implemented beyond Java exceptions distinguishing outcomes in-process (`InsufficientStockException`, `InventoryNotFoundException`, `InvalidInventoryException`). No structured logging, metrics, or external observability exists yet.

## 9. Invariant Verification

Each invariant must eventually have at least one explicit verification mechanism.

The expected relationship is:

    Invariant
        ↓
    Test
        ↓
    Concurrent execution
        ↓
    Result
        ↓
    Invariant verification

Critical invariants must be verified after concurrent and load-test executions.

A successful HTTP response is not sufficient evidence of correctness - and, at the current stage of this project, is not yet even applicable, since no HTTP layer exists.

## 10. Invariant Failure

Any implementation that violates a critical invariant must be considered incorrect, regardless of:

- Throughput
- Latency
- Resource utilization
- Architectural simplicity
- Scalability

Correctness is a prerequisite for performance optimization.

## 11. Critical Invariants

| #   | Invariant                            | Status                     |
| --- | ------------------------------------ | -------------------------- |
| 1   | `INV-001` - Non-Negative Inventory   | ✅ Proven                  |
| 2   | `INV-002` - No Overselling           | ✅ Proven                  |
| 3   | `INV-003` - Inventory Conservation   | ✅ Proven                  |
| 4   | `INV-004` - Atomic Allocation        | ✅ Proven                  |
| 5   | `INV-005` - Committed Success        | 🔶 Architecturally Aligned |
| 6   | `INV-006` - Rollback Preservation    | 🔶 Architecturally Aligned |
| 7   | `INV-010` - No Duplicate Allocation  | ⬜ Not Yet Implemented     |
| 8   | `INV-014` - Process Independence     | ✅ Proven                  |
| 9   | `INV-015` - Shared State Consistency | 🔶 Architecturally Aligned |
| 10  | `INV-016` - Idempotent Allocation    | ⬜ Not Yet Implemented     |

These invariants define the minimum correctness boundary
