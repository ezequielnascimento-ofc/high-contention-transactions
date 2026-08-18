<p align="right">
  <sub>
    <b>Language:</b>&nbsp;&nbsp;
    <a href="README.md">Português</a> /
    <a href="profile/english.md">English</a> /
    <a href="profile/spanish.md">Spanish</a> /
    <a href="profile/french.md">French</a>
  </sub>
</p>

# High-Contention Transactions

> Engineering transactional systems under high contention.

A Spring Boot project built to answer a concrete question: **can a system correctly handle thousands of concurrent requests competing for the same resource, without overselling or losing updates?**

The project simulates a classic e-commerce race condition, many simultaneous requests decrementing the same product's stock and proves, through automated concurrency tests, that the chosen persistence strategy holds under load.

## Highlights

- **Domain-Driven Design** with two bounded contexts (`Product`, `Inventory`), each isolated behind a Hexagonal Architecture (ports & adapters).
- **Test-Driven Development** across all layers: domain, application services, and infrastructure (JDBC repositories).
- **Concurrency correctness proven empirically**: an automated test suite fires **10, 100, 1.000 and 10.000** concurrent threads against a single inventory record and asserts zero overselling in every run, no negative stock, no lost updates.
- **Atomic conditional `UPDATE`** as the concurrency control strategy (`WHERE quantity >= :quantity`), avoiding the throughput cost of pessimistic locks and the retry complexity of optimistic locking.

## Tech Stack

- Java 21 · Spring Boot 4
- Spring JDBC (`JdbcClient`) - no ORM, full control over SQL and transaction boundaries
- PostgreSQL (production) · H2 (test)
- Flyway for schema migrations
- JUnit 5 + Mockito

## Status

Core domain complete and concurrency-tested. REST API layer in progress.

## License

MIT
