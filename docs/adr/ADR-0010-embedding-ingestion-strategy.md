# ADR-0010: Centralize Embeddings and Deterministic ADR Ingestion

* **Status:** Accepted
* **Date:** 2026-11-01
* **Deciders:** AS-Sentinel Team

## Context & Problem Statement

The AI service stores architecture rules in Qdrant and generates embeddings
with `sentence-transformers`. Separate vector-store implementations could use
different connection settings, models, or collection definitions.

## Decision

Use one `QdrantVectorStore` implementation for the API and ADR ingestion
script. The model, collection name, vector size, and Qdrant connection are
configured centrally. ADR ingestion uses deterministic identifiers so
re-running the command updates existing points instead of creating duplicates.

CPU and GPU dependency variants are maintained separately, while both use the
same embedding model and vector dimensions.

The Qdrant image is pinned in local Compose and CI so upgrades are deliberate
and can be validated against the client API.

## Consequences

* **Positive:** Consistent embeddings and Qdrant behavior across all entry
  points; repeatable ingestion; simpler testing and operations.
* **Negative/Risks:** Changing the embedding model or vector size requires
  migrating or recreating the collection and re-ingesting its documents.
