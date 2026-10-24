# Architecture Decision Records

This directory records the architectural decisions for AS-Sentinel. The records use the supplied architecture specification and the current repository as their sources of context. A **Proposed** decision describes intended architecture that has not yet been implemented; an **Accepted** decision is reflected in the current design or code.

| ADR | Decision | Status |
| --- | --- | --- |
| [ADR-0001](./ADR-0001-microservices.md) | Separate the Core and AI Engine into services | Accepted |
| [ADR-0002](./ADR-0002-core-ddd.md) | Structure the Core around Clean Architecture and DDD | Proposed |
| [ADR-0003](./ADR-0003-service-communication.md) | Use REST/JSON between the Core and AI Engine | Proposed |
| [ADR-0004](./ADR-0004-qdrant-vector-store.md) | Use Qdrant for vector storage and retrieval | Accepted |
| [ADR-0005](./ADR-0005-postgresql-metadata.md) | Use PostgreSQL for relational project data | Accepted |
| [ADR-0006](./ADR-0006-rag-audit-pipeline.md) | Analyze repositories through a retrieval-augmented pipeline | Proposed |
| [ADR-0007](./ADR-0007-security-baseline.md) | Apply layered security controls and OWASP-based audits | Proposed |
| [ADR-0008](./ADR-0008-testing-strategy.md) | Validate services with unit, integration, and AI evaluation tests | Proposed |

