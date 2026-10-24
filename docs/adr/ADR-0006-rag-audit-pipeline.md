# ADR-0006: Analyze Repositories Through a Retrieval-Augmented Pipeline

* **Status:** Proposed
* **Date:** 2026-10-24
* **Deciders:** Project maintainers

## Context & Problem Statement

Architecture and security audits need to analyze repository content while grounding results in relevant code and rules. Sending an entire repository directly to a language model would be inefficient and make findings harder to trace back to evidence.

The supplied architecture specification describes repository/file ingestion, chunking, embeddings, Qdrant indexing, retrieval, and AI-assisted audit results. It also names LlamaIndex and LangChain as possible orchestration libraries, but the current service has not selected or implemented either one.

## Decision

Build the AI audit workflow as a retrieval-augmented pipeline:

1. Accept an authorized repository or file set from the Core.
2. Chunk the source and create embeddings using a model selected in a later decision.
3. Index chunks and associated metadata in Qdrant.
4. Retrieve relevant code and rules for an audit request.
5. Return findings with enough source context for the Core to assemble an audit report.

Keep the orchestration library, model/provider, chunking policy, and evaluation thresholds open until they are validated against representative repositories. The current FastAPI and Qdrant endpoints are only initial infrastructure, not a complete implementation of this pipeline.

## Consequences

* **Positive:** Grounds analysis in retrieved project evidence, supports incremental indexing, and keeps embedding and retrieval concerns in the AI Engine.
* **Negative/Risks:** Adds ingestion, indexing, and evaluation complexity. Retrieval quality, model output, and source attribution must be measured; stale or incomplete indexes can lead to incorrect findings.

