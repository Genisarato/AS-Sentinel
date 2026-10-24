# ADR-0007: Apply Layered Security Controls and OWASP-Based Audits

* **Status:** Proposed
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

AS-Sentinel handles source code and project metadata, and its own services and container images also need protection. In addition, security audits should produce findings that are grounded in recognized vulnerability categories rather than ad hoc checks.

The supplied architecture specification calls for JWT authentication in the Core, OWASP Top 10-oriented security analysis, container image scanning with Trivy, and static analysis with Semgrep or SonarQube in GitHub Actions. These controls are not yet implemented in the current service scaffold.

## Decision

Adopt a layered security baseline as these capabilities are implemented:

* Protect authenticated Core operations with JWT-based authentication; define token issuance, validation, expiration, and rotation as part of the authentication design.
* Include OWASP Top 10 patterns in the security audit scope, including authorization failures, missing input sanitization, and hardcoded secrets.
* Add automated container image vulnerability scanning and static application security testing to CI before relying on the services for sensitive repositories.
* Treat repository access authorization and handling of source contents as explicit parts of ingestion workflows.

The specific JWT issuer, token rotation policy, CI thresholds, and scanner configuration remain to be decided before implementation. No current endpoint should be considered authenticated based on this ADR.

## Consequences

* **Positive:** Establishes a security baseline for both the product's audit purpose and its own services; makes security checks repeatable in development and CI.
* **Negative/Risks:** Adds operational and CI work, can produce false positives, and requires careful handling of credentials and source code. JWT alone does not provide authorization; access checks and secure secret management are also required.

