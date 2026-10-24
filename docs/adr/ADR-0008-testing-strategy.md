# ADR-0008: Validate Services with Unit, Integration, and AI Evaluation Tests

* **Status:** Proposed
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

AS-Sentinel spans Java and Python services, external HTTP calls, PostgreSQL, Qdrant, and AI-generated results. Unit tests alone cannot validate infrastructure integration or whether retrieval and generated audit results are useful.

The supplied architecture specification proposes JUnit 5, Mockito, AssertJ, and Testcontainers for Java; Pytest and pytest-mock for Python; and fixed-input AI evaluations to measure retrieval quality and reduce hallucinations. The repository currently includes a small Pytest suite, but the broader strategy and CI workflow are not in place.

## Decision

Use a layered testing strategy:

* **Unit tests:** Validate Java domain rules and Python service behavior with isolated dependencies and deterministic mocks.
* **Integration tests:** Exercise real PostgreSQL and Qdrant interactions in disposable environments, using Testcontainers where appropriate.
* **API tests:** Verify HTTP request/response behavior and error handling for each service and their integration contracts.
* **AI evaluations:** Maintain representative, fixed inputs and expected evidence or outcomes; track retrieval quality and unsupported or hallucinated results over time.
* Run relevant checks in CI, including linting and security scanning as described in ADR-0007.

AI evaluation thresholds should be established from a representative baseline rather than guessed in advance.

## Consequences

* **Positive:** Catches defects at the layer where they occur, verifies external-service integration, and provides a repeatable way to assess AI behavior.
* **Negative/Risks:** Integration and AI evaluations take additional time and infrastructure. Evaluation datasets require maintenance, and results can vary when models or prompts change.

