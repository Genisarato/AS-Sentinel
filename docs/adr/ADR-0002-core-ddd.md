# ADR-0002: Structure the Core around Clean Architecture and DDD

* **Status:** Proposed
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

The Core will coordinate projects, repository sources, scan configuration, audit reports, findings, and rule violations. Keeping those domain concepts independent of web, persistence, and AI integrations is important as the audit workflows grow.

The supplied architecture specification proposes Clean Architecture and Domain-Driven Design (DDD), with Project Management and Audit Engine bounded contexts. The current Java service is still a small Spring Boot scaffold and does not yet contain these domain models or use cases.

## Decision

Structure the Core using Clean Architecture and DDD:

* Keep domain models and domain services independent of frameworks and infrastructure.
* Put application workflows in use cases, including repository ingestion, architecture-rule evaluation, and security scanning.
* Treat Project Management and Audit Engine as the initial bounded contexts.
* Model concepts such as `Project`, `RepositorySource`, `ScanConfig`, `AuditReport`, `Finding`, and `RuleViolation` in the domain, using value objects where appropriate.
* Integrate persistence, HTTP endpoints, and external services through infrastructure adapters.

This is a proposed target structure; it should be adopted as the Core's domain behavior is implemented.

## Consequences

* **Positive:** Protects business rules from framework changes, clarifies ownership of domain behavior, and makes use cases easier to test.
* **Negative/Risks:** Adds structure and concepts that may be excessive for trivial features; boundaries and models must be maintained rather than becoming empty layers.

