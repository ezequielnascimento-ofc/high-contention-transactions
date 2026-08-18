# Security Baseline

## 1. Purpose

This document defines the security baseline for the project.

Security is treated as a development constraint rather than a final validation step.

The system should be secure by default, minimizing the likelihood that insecure behavior is introduced through application code, configuration, dependencies, infrastructure, or development practices.

This document defines the security rules and principles that govern development going forward. Each section includes a note on the current implementation state, distinguishing between rules that are already enforced and areas that remain pending, the rules themselves are not conditional on implementation progress.

## 2. Security Principles

The project follows these principles:

- Secure by default
- Least privilege
- Defense in depth
- Explicit trust boundaries
- Fail securely
- Minimize attack surface
- Never trust external input
- Never commit secrets
- Minimize sensitive data exposure
- Prefer secure configuration over developer discipline

## 3. Secrets Management

Secrets must never be committed to the repository.

This includes:

- Passwords
- API keys
- Access tokens
- Database credentials
- Private keys
- Certificates containing private material
- Session secrets

Local development must use environment variables or an equivalent external configuration mechanism.

Example:

    DATABASE_USERNAME
    DATABASE_PASSWORD
    REDIS_PASSWORD

Example configuration files containing real credentials must never be committed.

A sanitized example configuration may be provided when necessary.

**Current state.** No real secrets exist in the repository. `application-test.yaml` uses a local, disposable H2 in-memory database with a placeholder credential, which is acceptable for a test-only, ephemeral database. Production configuration with real PostgreSQL credentials has not been defined yet, so externalization for a production environment has not been validated.

## 4. Configuration

Configuration must be externalized from application code.

Sensitive configuration must not have insecure hardcoded defaults.

Development defaults may be provided only when they do not create a meaningful security risk.

Production configuration must explicitly define security-sensitive values.

**Current state.** `application.yaml` (production) and `application-test.yaml` (test) exist and are separated by Spring profile. Production-specific secrets sourcing, such as environment variables for PostgreSQL credentials, has not been implemented, since there is no production deployment yet to validate against.

## 5. Input Validation

All externally controlled input must be considered untrusted.

The application must validate:

- Required fields
- Data types
- Numeric ranges
- String lengths
- Identifiers
- Requested quantities
- Pagination parameters, where applicable

Validation must occur before the input reaches business or persistence operations.

**Current state.** Domain-level validation exists and is enforced: `Product` and `Inventory` reject invalid state (blank names, negative prices, non-positive quantities) through their own constructors and mutators. Request-level validation does not exist yet, since no HTTP layer or DTOs have been built. This gap is also tracked in `requirements.md`, under SR-003.

## 6. Resource Limits

Externally controlled values must have reasonable limits.

Examples include:

- Maximum allocation quantity
- Maximum request payload size
- Maximum identifier length
- Maximum pagination size
- Maximum request processing time

Limits must prevent accidental or malicious resource exhaustion.

**Current state.** Not implemented. No limits exist yet, since there is no external-facing endpoint to enforce them on.

## 7. Database Security

The application database user should follow the principle of least privilege.

The application should not require administrative database privileges.

Database credentials must be supplied externally.

Database access must use parameterized queries or framework mechanisms that prevent SQL injection.

Schema modification privileges should be separated from normal application runtime privileges where practical.

**Current state.** All database access goes through Spring's `JdbcClient` with named parameter binding. No string-concatenated SQL exists anywhere in the codebase, so SQL injection is structurally prevented by construction, not by developer discipline alone. Least-privilege database user separation between the application role and the migration/admin role has not been evaluated yet, since there is no production database provisioned.

## 8. Redis Security

Redis must not be exposed publicly.

When Redis is introduced, access should be restricted to trusted application components.

Authentication and network isolation must be evaluated according to the deployment environment.

Redis must not be considered a trusted source of inventory correctness unless explicitly justified by an architectural decision.

**Current state.** Not applicable. Redis has not been introduced. The atomic conditional `UPDATE` strategy resolved the core correctness problem at the database level without requiring an external coordination layer, as documented in `problem.md`.

## 9. Authentication and Authorization

Authentication and authorization are outside the initial domain scope unless required by a concrete system requirement.

If endpoints are intentionally exposed without authentication for benchmarking purposes, this must be an explicit design decision.

Benchmark accessibility must not be confused with a production security model.

**Current state.** Not applicable yet. No HTTP endpoints exist. This section must be revisited before any endpoint introduced by the planned REST API layer is considered ready for anything beyond local benchmarking.

## 10. API Security

External endpoints must:

- Validate input
- Return appropriate HTTP status codes
- Avoid exposing internal implementation details
- Avoid returning stack traces
- Enforce reasonable request limits
- Avoid unnecessary information disclosure

Error responses should provide enough information for clients to understand the failure without exposing internal system details.

**Current state.** Not applicable yet, since no API exists. This section becomes active as soon as the Controller layer is implemented, and is directly tied to the `GlobalExceptionHandler` work already planned.

## 11. Error Handling

Internal exceptions must not be exposed directly to clients.

The application must separate:

    Internal diagnostic information

from:

    External error response

Detailed diagnostic information should remain available through controlled application logs and observability mechanisms.

**Current state.** Not implemented. There is no `GlobalExceptionHandler` yet. This is a known, explicitly tracked gap (see `requirements.md`, SR-009) and is the next planned implementation step for the project.

## 12. Logging

Logs must not contain:

- Passwords
- Access tokens
- API keys
- Private keys
- Sensitive credentials

Logs should provide sufficient information to diagnose:

- Transaction failures
- Concurrency failures
- Dependency failures
- Unexpected application errors

Structured logging should be preferred where practical.

**Current state.** Only Spring Boot's default logging is in place, covering startup, Flyway migrations, and HikariCP pool events. No structured logging exists, and no formal review has been conducted — there is currently nothing sensitive flowing through any logged path, since no credentials or secrets are logged.

## 13. Dependency Security

Project dependencies must be periodically evaluated for known vulnerabilities.

The development process should include automated dependency vulnerability scanning.

Dependencies should be:

- Explicitly declared
- Version controlled
- Kept reasonably up to date
- Removed when no longer required

A dependency should not be introduced solely for convenience when the same functionality can be implemented safely without unnecessary complexity.

**Current state.** Dependencies are explicitly declared in `pom.xml` and version-controlled. No automated vulnerability scanning, such as OWASP Dependency-Check or GitHub Dependabot, is configured yet.

## 14. Container Security

Containers should follow secure-by-default practices.

Where practical:

- Use minimal base images
- Avoid unnecessary packages
- Do not run the application as root
- Pin important image versions
- Do not embed secrets in images
- Minimize exposed ports
- Keep runtime permissions restricted

Docker configuration should be treated as part of the application's security boundary.

**Current state.** Not applicable yet. No Dockerfile or container image exists.

## 15. Source Control Security

The repository must prevent accidental secret disclosure.

The development workflow should include:

- `.gitignore` protection
- Secret scanning
- Dependency scanning
- Static analysis where appropriate

If a secret is accidentally committed, removing it from the latest commit is not sufficient.

The credential must be considered compromised and rotated.

**Current state.** A `.gitignore` is in place, covering standard Maven and build-output exclusions. No automated secret scanning or static analysis is configured yet.

## 16. CI/CD Security

CI/CD workflows must follow least privilege.

Workflow credentials should have only the permissions required by the workflow.

Third-party GitHub Actions should be evaluated before being introduced.

Secrets must be provided through the CI/CD secret mechanism rather than committed to workflow files.

Build pipelines must not print sensitive environment variables.

**Current state.** Not applicable yet. No CI/CD pipeline has been configured.

## 17. Dependency and Supply Chain Security

The project should evaluate the software supply chain, including:

- Application dependencies
- Container images
- Build plugins
- GitHub Actions
- Development tools

Security scanning should be integrated into CI where practical.

**Current state.** Not implemented. This depends on the dependency scanning and CI/CD work described in Sections 13 and 16 being addressed first.

## 18. Security Testing

Security verification should include, where applicable:

- Dependency vulnerability scanning
- Static analysis
- Input validation tests
- Authentication and authorization tests when applicable
- Injection testing
- Error handling tests
- Secret detection
- Container configuration checks

Security testing must be proportional to the actual attack surface.

**Current state.** Not implemented as dedicated security tests. Domain-level input validation is covered by unit tests (`ProductTest`, `InventoryTest`), which incidentally overlap with input validation testing, but no test is explicitly framed or scoped as a security test.

## 19. Development Rules

The following rules apply throughout development, regardless of implementation stage:

1. Never commit secrets.
2. Never trust external input.
3. Never rely on client-side validation for security.
4. Never use administrative database credentials for application runtime.
5. Never expose internal exceptions directly to clients.
6. Never introduce a security-sensitive dependency without understanding its behavior.
7. Never disable a security control merely to simplify development without documenting the reason.
8. Never treat benchmark configuration as a production security configuration.
9. Prefer secure defaults over optional security controls.
10. Security regressions must be treated as engineering defects.

## 20. Security vs Performance

Security controls must not be removed solely because they introduce measurable overhead.

If a security mechanism has a relevant performance impact, the trade-off must be measured and documented.

The objective is not maximum performance at any cost.

The objective is:

    Correctness
        +
    Security
        +
    Performance

with explicit trade-offs where these concerns conflict.

**Current state.** No conflict has arisen so far. The concurrency-control strategy adopted, an atomic conditional `UPDATE`, was selected on correctness and performance grounds simultaneously, as documented in `problem.md`, and introduces no known security trade-off.

## 21. Security Review

Security considerations must be evaluated when introducing:

- New endpoints
- New dependencies
- New infrastructure
- New persistence mechanisms
- New external integrations
- New authentication mechanisms
- New concurrency-control mechanisms

Security is part of architectural review rather than a final project phase.

**Current state.** Applied so far for the one architectural decision made to date: the concurrency-control mechanism, documented in `problem.md` with its correctness reasoning. No new endpoints, dependencies, or infrastructure have been introduced since.
