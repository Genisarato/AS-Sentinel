# ADR-0003: Use REST/JSON Between the Core and AI Engine

* **Status:** Proposed
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

The Core must request AI capabilities without taking a dependency on Python implementation details. The services need a transport that is straightforward to expose, test, and document.

The supplied architecture specification shows the Core and AI Engine communicating over HTTP REST with JSON. The AI service already exposes FastAPI HTTP endpoints, but the Core does not yet call them.

## Decision

Use versioned REST/JSON APIs for synchronous communication between the Core and AI Engine. The Core should depend on an explicit API contract rather than importing or relying on AI Engine internals. Keep service-specific endpoints versioned and document request and response schemas.

This decision does not define asynchronous job handling, retries, authentication between services, or the complete API contract; those should be decided when the corresponding workflows are implemented.

## Consequences

* **Positive:** Uses a familiar, language-neutral interface that fits the current FastAPI service and is easy to exercise with HTTP tests.
* **Negative/Risks:** Synchronous calls couple request availability to both services and require timeouts, error mapping, and contract compatibility. Large repository scans may eventually need an asynchronous interaction pattern.

