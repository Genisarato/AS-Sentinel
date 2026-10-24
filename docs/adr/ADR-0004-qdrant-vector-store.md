# ADR-0004: Use Qdrant for Vector Storage and Retrieval

* **Status:** Accepted
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

Semantic retrieval over repository chunks and architecture rules requires a store that supports vector similarity search. The architecture specification selects Qdrant, and the repository runs it through Docker Compose and connects to it from the AI service.

The current AI configuration names the `architecture_rules` collection, sets its vector size to 384, and initializes cosine distance. The project has not yet implemented the complete embedding or repository-ingestion pipeline.

## Decision

Use Qdrant as the vector store for the AI Engine. The initial `architecture_rules` collection uses 384-dimensional vectors and cosine distance, as configured in the current service.

The embedding model and its production configuration remain undecided. Before indexing real data, the project must select an embedding model and keep its output dimensions and preprocessing consistent with the collection configuration.

## Consequences

* **Positive:** Provides a dedicated vector-search service and keeps vector retrieval separate from relational project metadata.
* **Negative/Risks:** Adds an operational dependency and requires collection lifecycle, embedding compatibility, persistence, and error handling to be managed. Changing vector dimensions or models may require reindexing.

