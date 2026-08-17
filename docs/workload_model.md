# Workload Model

## 1. Purpose

This document defines the workload characteristics used to evaluate the system under concurrent and high-contention conditions.

The workload model establishes reproducible conditions for concurrency, contention, request volume, resource availability, and test duration.

Performance results must not be interpreted independently from the workload that produced them.

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

The initial benchmark model will use single-unit allocation:

    requested_quantity = 1

This simplifies the correctness model and allows concurrency behavior to be isolated.

Multi-unit allocation may be introduced later to evaluate whether the concurrency-control strategy remains correct when requests compete for different quantities.

## 9. Request Arrival Patterns

The workload model will distinguish between different request arrival patterns.

### Burst

A large number of requests arrive within a very short period.

    Requests  │██████████████████████████
              └──────────────────────────→ time

This represents flash-sale-like traffic.

### Sustained Load

Requests arrive continuously over a defined period.

    Requests  │██████████████████████████
              └──────────────────────────→ time

This is useful for observing resource saturation and steady-state behavior.

### Ramp-Up

Concurrency gradually increases until the system reaches a defined limit.

    Concurrency
        │
        │        /
        │      /
        │    /
        │  /
        │/
        └──────────────────→ time

This workload is useful for identifying saturation points.

## 10. Initial Test Scenarios

The following scenarios establish the initial experimental matrix.

### Scenario A — Correctness Baseline

Purpose:

Validate the basic transactional behavior without extreme contention.

    initial_stock       = 1,000
    allocation_attempts = 1,000
    requested_quantity  = 1

Expected:

    successful_allocations = 1,000
    final_stock             = 0
    overselling             = 0

### Scenario B — Oversupply

Purpose:

Validate overselling prevention when demand exceeds supply.

    initial_stock       = 1,000
    allocation_attempts = 10,000
    requested_quantity  = 1

Expected:

    successful_allocations <= 1,000
    final_stock >= 0
    overselling = 0

### Scenario C — Extreme Contention

Purpose:

Evaluate behavior when many operations compete for the same resource.

    initial_stock       = 1
    allocation_attempts = 10,000
    requested_quantity  = 1

Expected:

    successful_allocations = 1
    final_stock             = 0
    overselling             = 0

### Scenario D — Demand Below Supply

Purpose:

Evaluate the system when sufficient inventory exists for every request.

    initial_stock       = 10,000
    allocation_attempts = 1,000
    requested_quantity  = 1

Expected:

    successful_allocations = 1,000
    final_stock             = 9,000
    overselling             = 0

### Scenario E — Ramp-Up

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

The saturation point must be determined experimentally.

## 11. Baseline Environment

Every benchmark must record the environment in which it was executed.

At minimum:

- Operating system
- CPU
- Available memory
- Java version
- JVM configuration
- Spring Boot version
- PostgreSQL version
- Redis version, when applicable
- Database configuration
- Connection pool configuration
- Application instance count
- Load-testing tool and version

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

## 13. Repetitions

A benchmark must not rely on a single execution.

Experiments should be repeated sufficiently to identify variability.

Results should distinguish between:

- Minimum
- Maximum
- Median
- Percentiles where appropriate

Latency should primarily be evaluated using percentiles rather than averages alone.

## 14. Correctness Verification

Every concurrency and load test must verify the system invariants after execution.

At minimum:

    successful_allocations <= initial_stock

    final_stock >= 0

and, where applicable:

    successful_allocations + final_stock = initial_stock

Performance results from an implementation that violates a critical invariant must be considered invalid.

A faster incorrect implementation is not an improvement.

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
