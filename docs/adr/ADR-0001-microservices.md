# ADR-0001: Separate the Core and AI Engine into Services

* **Status:** Accepted
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

AS-Sentinel needs to coordinate project management and audit workflows with code ingestion, embedding generation, and AI-assisted analysis. These responsibilities have different runtime and dependency needs: the Core is a Java service, while the AI Engine uses the Python ecosystem.

The supplied architecture specification describes two independently deployable services. The repository also has separate `as-sentinel-core` and `as-sentinel-ai` applications.

## Decision

AS-Sentinel is organized as two services:

* **Core (`as-sentinel-core`):** owns project and audit workflows and is implemented in Java.
* **AI Engine (`as-sentinel-ai`):** owns AI-related processing and vector-store operations and is implemented in Python.

Each service can evolve independently while exposing an explicit interface to the other. This decision records the service boundary; it does not imply that all planned workflows or inter-service calls are implemented.

## Consequences

* **Positive:** Keeps Java domain and project workflows separate from Python AI dependencies; allows each service to be tested and deployed independently.
* **Negative/Risks:** Requires service-to-service contracts, network failure handling, observability, and coordinated operational configuration. The current repository is an early scaffold and does not yet implement the complete workflow.

