# ADR-0005: Use PostgreSQL for Relational Project Data

* **Status:** Accepted
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

AS-Sentinel needs durable relational storage for project, repository, scan configuration, user, and audit metadata. Those records have relationships and transactional updates that are distinct from semantic vector retrieval.

The supplied architecture specification assigns relational metadata to PostgreSQL. The repository configures PostgreSQL 16 in Docker Compose and the Core includes JPA and a PostgreSQL driver. Domain persistence models have not yet been implemented.

## Decision

Use PostgreSQL as the relational database owned by the Core for project and audit metadata. Keep semantic vectors in Qdrant rather than duplicating vector-search responsibilities in PostgreSQL.

The current Compose credentials and local connection settings are for development only. Production credentials and deployment configuration must be supplied securely outside source-controlled defaults.

## Consequences

* **Positive:** Provides transactional relational storage that integrates with the Core's Java/JPA stack and complements Qdrant's retrieval role.
* **Negative/Risks:** Requires schema evolution, backup and migration practices, and separate operations from the vector store. Local development configuration must not be reused as production configuration.

