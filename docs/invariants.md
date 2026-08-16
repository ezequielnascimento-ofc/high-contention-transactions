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

## 2. Inventory Invariants

### INV-001 - Non-Negative Inventory

Available inventory must never become negative.

```text
available_stock >= 0
```

---

### INV-002 - No Overselling

The total quantity successfully allocated must never exceed the initial available quantity.

    successful_allocated_quantity <= initial_stock

For a single-unit allocation model:

    successful_allocations <= initial_stock

---

### INV-003 - Inventory Conservation

For a resource whose only state transition is successful allocation:

    initial_stock = final_stock + successfully_allocated_quantity

Example:

    Initial stock:              1,000
    Successful allocations:      723
    Final stock:                  277

Therefore:

    1,000 = 723 + 277

This invariant must hold after the system reaches a committed stable state.

---

### INV-004 - Atomic Allocation

A successful allocation must consume the complete requested quantity.

An allocation must not result in a partially committed resource consumption unless partial allocation is explicitly supported by the domain model.

The operation must therefore have an atomic outcome:

    SUCCESS  → requested quantity consumed
    REJECTED → no quantity consumed
    FAILED   → no committed quantity consumed

## 3. Transaction Invariants

### INV-005 - Committed Success

A successful allocation must correspond to a committed state transition.

The system must not report:

    SUCCESS

when the corresponding resource mutation was rolled back or never committed.

---

### INV-006 - Rollback Preservation

If an allocation transaction is rolled back, the allocation must not permanently reduce available inventory.

Before:

    stock = N

After rollback:

    stock = N

---

### INV-007 - Isolation From Uncommitted State

A transaction must not make correctness decisions based on invalid or uncommitted state.

The implementation must explicitly define the transaction isolation behavior required to preserve the system invariants.

## 4. Concurrency Invariants

### INV-008 - Concurrency Independence

System correctness must not depend on the order in which concurrent requests are executed.

Given the same initial state and the same set of valid allocation requests, different transaction interleavings must not produce an invalid committed state.

---

### INV-009 - No Lost Allocation

A successfully committed allocation must not be silently overwritten by another concurrent operation.

Every committed successful allocation must be reflected in the resulting resource state.

---

### INV-010 - No Duplicate Allocation

The same logical allocation request must not consume the resource more than once when the system's idempotency contract requires duplicate requests to represent the same operation.

## 5. Failure Invariants

### INV-011 - Application Failure Safety

An application failure must not create a committed inventory state that violates the inventory invariants.

---

### INV-012 - Database Failure Safety

Database failures must not result in a committed state that violates the inventory invariants.

The system must explicitly define the observable outcome when the result of a transaction cannot be determined by the client.

---

### INV-013 - Retry Safety

Retrying an operation must not cause additional resource consumption beyond what is permitted by the operation's idempotency semantics.

## 6. Multi-Instance Invariants

### INV-014 - Process Independence

Correctness must not depend on JVM-local synchronization mechanisms.

The following must not be required for correctness:

- `synchronized`
- JVM-local locks
- Local mutexes
- In-memory inventory state

Such mechanisms may be evaluated experimentally, but they cannot be the foundation of correctness when multiple application instances are supported.

---

### INV-015 - Shared State Consistency

Multiple application instances operating concurrently against the same resource must preserve all system invariants.

Example:

    Instance A ─┐
    Instance B ─┼──> Shared Resource
    Instance C ─┘

The final state must remain valid regardless of which instance processes each request.

## 7. Idempotency Invariants

### INV-016 - Idempotent Allocation

When an allocation request is identified as idempotent, processing the same logical request multiple times must produce the same effective resource consumption as processing it once.

For example:

    Request ID: A123

    First request → SUCCESS → consume 1 unit
    Retry         → SUCCESS/REPLAYED → consume 0 additional units

The exact API semantics will be defined separately.

## 8. Observability Invariants

### INV-017 - Outcome Traceability

Every allocation attempt must produce an observable outcome that allows the system to distinguish, where applicable:

- Successful allocation
- Rejected allocation
- Validation failure
- Transaction failure
- Dependency failure
- Timeout
- Duplicate request

Observability must not compromise security or expose sensitive information.

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

A successful HTTP response is not sufficient evidence of correctness.

## 10. Invariant Failure

Any implementation that violates a critical invariant must be considered incorrect, regardless of:

- Throughput
- Latency
- Resource utilization
- Architectural simplicity
- Scalability

Correctness is a prerequisite for performance optimization.

## 11. Critical Invariants

The following invariants are considered critical:

1. `INV-001` - Non-Negative Inventory
2. `INV-002` - No Overselling
3. `INV-003` - Inventory Conservation
4. `INV-004` - Atomic Allocation
5. `INV-005` - Committed Success
6. `INV-006` - Rollback Preservation
7. `INV-010` - No Duplicate Allocation
8. `INV-014` - Process Independence
9. `INV-015` - Shared State Consistency
10. `INV-016` - Idempotent Allocation

These invariants define the minimum correctness boundary for the system.
