# Workload Model

## 1. Purpose

This document defines the workload characteristics used to evaluate the system under concurrent and high-contention conditions.

The workload model establishes reproducible conditions for concurrency, contention, request volume, resource availability, and test duration.

Performance results must not be interpreted independently from the workload that produced them.

### Status Legend

- ✅ Executed - this workload has been run and produced verified results.
- ⬜ Planned - defined in the model, not yet executed.

## 2. Workload Dimensions

The workload will be characterized by the following dimensions:

- Initial resource quantity
- Number of allocation requests
- Concurrency level
- Contention ratio
- Request arrival pattern
- Test duration
- Allocation quantity
- Number of application instances
- Database configuration
- Connection pool configuration

## 3. Initial Resource Quantity

The initial resource quantity represents the amount of inventory available before a test begins.

The initial value must be explicitly defined for every experiment.

Example:

    initial_stock = 1,000

The initial quantity must be sufficient to create scenarios where:

- Demand is below supply
- Demand approaches supply
- Demand exceeds supply
- Extreme contention occurs on a nearly exhausted resource

## 4. Request Volume

Request volume represents the total number of allocation attempts submitted during an experiment.

Example:

    allocation_attempts = 10,000

Request volume must be distinguished from concurrency.

A test with 10,000 requests does not necessarily mean that 10,000 requests execute simultaneously.

## 5. Concurrency

Concurrency represents the maximum number of operations actively executing at the same time.

Example:

    requests = 10,000
    concurrency = 500

This means that 10,000 allocation attempts are submitted using a maximum concurrency of 500 active operations.

Concurrency must be treated as an independent experimental variable.

## 6. Contention

Contention represents the degree to which concurrent operations compete for the same shared resource.

The primary high-contention scenario targets a single resource.

Example:

    resources = 1
    concurrent_operations = N

Lower-contention scenarios may distribute requests across multiple resources.

This allows the system to distinguish between:

    high concurrency

and:

    high contention

A high number of concurrent requests does not necessarily imply high contention.

## 7. Demand-to-Supply Ratio

The relationship between requested quantity and available quantity is a primary workload dimension.

Define:

    demand_supply_ratio = total_requested_quantity / initial_stock

Example:

    initial_stock = 1,000
    total_requested_quantity = 10,000

    demand_supply_ratio = 10

The workload should include at least:

### Under-demand

    demand < supply

### Near-demand

    demand ≈ supply

### Over-demand

    demand > supply

The over-demand scenario is particularly important for validating overselling prevention.

## 8. Allocation Quantity

The initial benchmark model uses single-unit allocation:

    requested_quantity = 1

This simplifies the correctness model and allows concurrency behavior to be isolated. This is the model used by the executed experiment (Section 10, Scenario A′).

Multi-unit allocation may be introduced later to evaluate whether the concurrency-control strategy remains correct when requests compete for different quantities.

## 9. Request Arrival Patterns

The workload model distinguishes between different request arrival patterns.

### Burst ✅

A large number of requests arrive within a very short period.

    Requests  │██████████████████████████
              └──────────────────────────→ time

This represents flash-sale-like traffic. **This is the pattern used by the executed experiment** - all threads are released simultaneously via a `CountDownLatch`, producing a true burst rather than a staggered arrival.

### Sustained Load ⬜

Requests arrive continuously over a defined period.

    Requests  │██████████████████████████
              └──────────────────────────→ time

This is useful for observing resource saturation and steady-state behavior. Not yet executed.

### Ramp-Up ⬜

Concurrency gradually increases until the system reaches a defined limit.

    Concurrency
        │
        │        /
        │      /
        │    /
        │  /
        │/
        └──────────────────→ time

This workload is useful for identifying saturation points. Not yet executed.

## 10. Test Scenarios

The following scenarios establish the experimental matrix. Scenario A′ has been executed; the remainder are planned.

### Scenario A′ - Exact Demand-Supply Contention ✅ Executed

Purpose:

Validate overselling prevention under a demand-supply ratio of exactly 2:1, at increasing concurrency levels, using an in-process burst workload.

This is the scenario actually implemented by `DecreaseStockConcurrencyTest`, run independently at four concurrency levels:

    concurrency         = 10, 100, 1,000, 10,000  (one run per value)
    initial_stock       = concurrency / 2
    allocation_attempts = concurrency
    requested_quantity  = 1
    arrival_pattern     = burst (CountDownLatch-synchronized)
    execution_layer     = in-process (application service, not HTTP)

Result, for every concurrency level tested:

    successful_allocations = concurrency / 2   (exactly)
    rejected_allocations   = concurrency / 2   (InsufficientStockException)
    final_stock             = 0
    overselling             = 0

Deviation from the model as originally proposed: this scenario tests exactly at the 2:1 demand-supply ratio rather than the 10:1 ratio used in Scenario B below, and it exercises the application layer directly rather than an HTTP-facing endpoint, since no Controller exists yet. The result is nonetheless a direct verification of `INV-001`, `INV-002`, `INV-003`, `INV-004`, `INV-008`, and `INV-009` (see `invariants.md`).

### Scenario B - Oversupply ⬜ Planned

Purpose:

Validate overselling prevention when demand exceeds supply by a wider margin than Scenario A′.

    initial_stock       = 1,000
    allocation_attempts = 10,000
    requested_quantity  = 1

Expected:

    successful_allocations <= 1,000
    final_stock >= 0
    overselling = 0

Not yet executed. Expected to hold based on the correctness reasoning already validated in Scenario A′, since the underlying mechanism (atomic conditional `UPDATE`) does not depend on the specific demand-supply ratio.

### Scenario C - Extreme Contention (Single Unit) ⬜ Planned

Purpose:

Evaluate behavior when many operations compete for a single remaining unit - the most extreme contention case.

    initial_stock       = 1
    allocation_attempts = 10,000
    requested_quantity  = 1

Expected:

    successful_allocations = 1
    final_stock             = 0
    overselling             = 0

Not yet executed. This is a stricter version of Scenario A′ (2:1 ratio) at a 10,000:1 ratio, and is expected to be the most demanding correctness test in the matrix.

### Scenario D - Demand Below Supply ⬜ Planned

Purpose:

Evaluate the system when sufficient inventory exists for every request.

    initial_stock       = 10,000
    allocation_attempts = 1,000
    requested_quantity  = 1

Expected:

    successful_allocations = 1,000
    final_stock             = 9,000
    overselling             = 0

Not yet executed.

### Scenario E - Ramp-Up ⬜ Planned

Purpose:

Identify the relationship between concurrency and system saturation.

Concurrency should gradually increase while monitoring:

- Throughput
- p50 latency
- p95 latency
- p99 latency
- Error rate
- Database utilization
- Connection pool utilization
- CPU
- Memory

The saturation point must be determined experimentally. Not yet executed - this scenario requires the performance instrumentation described in `requirements.md`, Section 4, which has not been built yet.

## 11. Baseline Environment

Every benchmark must record the environment in which it was executed.

Recorded for the executed experiment (Scenario A′):

    Database                : H2 (in-memory, MODE=PostgreSQL), not PostgreSQL directly
    Connection pool         : HikariCP, maximum-pool-size = 50
    Test framework          : JUnit 5 (@ParameterizedTest)
    Concurrency mechanism   : java.util.concurrent.ExecutorService (fixed thread pool, capped at 500) + CountDownLatch
    Execution layer         : in-process (DecreaseStockService called directly, no HTTP)

Not yet recorded, pending a dedicated benchmark run against production-equivalent infrastructure:

- Operating system
- CPU
- Available memory
- Java version (JVM used for the test run)
- JVM configuration (heap size, GC)
- Spring Boot version
- PostgreSQL version (production target, not yet used in concurrency testing)
- Application instance count
- Load-testing tool and version (no external load-testing tool has been used yet - concurrency was driven from within the JUnit test itself)

This gap is intentional at this stage: the executed experiment was designed to validate **correctness** under concurrency, not to produce a performance baseline. Section 4 of `requirements.md` tracks the performance-measurement work as not yet started.

## 12. Benchmark Isolation

Performance experiments should minimize unrelated system activity.

Whenever possible:

- Use a dedicated environment
- Avoid background workloads
- Keep software versions fixed
- Keep configuration fixed
- Reset the dataset between experiments
- Run multiple iterations
- Report representative results

The same workload must be used when comparing different implementations.

**Current state.** The executed experiment resets its own dataset per run - each `@ParameterizedTest` iteration creates a fresh `Product` and `Inventory` and removes them via `@AfterEach`, so no cross-run contamination occurs. Multiple iterations of the same concurrency level have not yet been run to measure variability (see Section 13).

## 13. Repetitions

A benchmark must not rely on a single execution.

Experiments should be repeated sufficiently to identify variability.

Results should distinguish between:

- Minimum
- Maximum
- Median
- Percentiles where appropriate

Latency should primarily be evaluated using percentiles rather than averages alone.

**Current state.** Each concurrency level (10, 100, 1,000, 10,000) has been executed once, as a correctness check rather than a statistical benchmark. No repeated runs exist yet to characterize variability, and no latency measurements have been collected - the test currently asserts correctness of final state, not timing.

## 14. Correctness Verification

Every concurrency and load test must verify the system invariants after execution.

At minimum:

    successful_allocations <= initial_stock

    final_stock >= 0

and, where applicable:

    successful_allocations + final_stock = initial_stock

Performance results from an implementation that violates a critical invariant must be considered invalid.

A faster incorrect implementation is not an improvement.

**Current state.** All three conditions above are explicitly asserted in `DecreaseStockConcurrencyTest` for every executed concurrency level, and all three held in every run. See `invariants.md` for the full mapping between this verification and the formal invariants (`INV-001` through `INV-004`).

## 15. Experimental Principle

Performance comparisons must change one significant variable at a time whenever practical.

For example:

    Same workload
        ↓
    Strategy A
        ↓
    Measure
        ↓
    Strategy B
        ↓
    Measure

The objective is to isolate the impact of the concurrency-control strategy.

**Current state.** Only one concurrency-control strategy has been implemented and measured so far - the atomic conditional `UPDATE` (see `problem.md` for the comparison reasoning against pessimistic and optimistic locking, which was made analytically, not experimentally). A controlled experimental comparison against alternative strategies has not been executed.

## 16. Future Workloads

Additional workloads may be introduced to evaluate:

- Multiple resources
- Multiple hot resources
- Uneven resource popularity
- Multi-unit allocation
- Request retries
- Timeouts
- Database contention
- Connection pool saturation
- Multiple application instances
- Dependency failures
- Sustained traffic
- Increasing concurrency

New workloads must have an explicit engineering question they are intended to answer.

## Important Note

The system's final capacity is not defined by the initial workload scenarios.

For example, 10,000 requests is an experimental workload, not a requirement that the system must support 10,000 simultaneous users.

User count, request volume, concurrency, and contention represent different dimensions of system behavior.

The workload model therefore maintains the following distinction:

    Request Volume
        ≠
    Concurrency
        ≠
    Contention
        ≠
    Throughput
