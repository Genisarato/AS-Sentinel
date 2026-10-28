# ADR-0009: GPU Acceleration and Python Runtime Environment

* **Status:** Accepted
* **Date:** 2026-10-28
* **Deciders:** AS-Sentinel Team

## Context & Problem Statement

The `as-sentinel-ai` service generates text embeddings with `sentence-transformers` and performs semantic searches over architecture rules and documentation using Qdrant. Local hardware acceleration is preferred to CPU-only processing to improve embedding and inference performance.

The initial setup also exposed compatibility problems with experimental Python versions such as Python 3.15. PyTorch C++ binaries and CUDA wheels were not consistently compiled or available for those versions through PyPI.

## Decision

1. Standardize the `as-sentinel-ai` runtime on Python 3.11.
2. Use PyTorch with the official CUDA 12.4 wheel repository to support local NVIDIA RTX-series GPU acceleration.
3. Use `sentence-transformers` for embedding generation.
4. Configure embedding execution to use `cuda` when CUDA is available and fall back to `cpu` when it is not.

The CUDA-enabled PyTorch wheel and its package source are declared in [`as-sentinel-ai/requirements.txt`](../../as-sentinel-ai/requirements.txt).

## Consequences

* **Positive:**
  * Faster vector embedding generation for large sets of ADRs and codebase snippets.
  * Native compatibility with PyTorch, `sentence-transformers`, FastAPI, and `qdrant-client`.
  * More deterministic local development environments by standardizing the Python runtime and PyTorch wheel.

* **Negative/Risks:**
  * The CUDA-enabled PyTorch wheel must be installed from the official PyTorch wheel repository.
  * The environment requires an NVIDIA GPU and compatible drivers to benefit from acceleration.
  * CUDA-enabled PyTorch dependencies increase the initial environment size by approximately 2 GB.
