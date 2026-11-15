# ADR-0012: Use Ollama and Qwen2.5 Coder for Local Architecture Evaluation

* **Status:** Accepted
* **Date:** 2026-11-15
* **Deciders:** Project maintainers

## Context & Problem Statement

The AI service needs a local language model to evaluate AST file summaries
against the architectural rules retrieved from Qdrant. The project is
personal and should support local execution without sending source code to an
external model provider.

## Decision

Use Ollama as the local model runtime and
`qwen2.5-coder:7b` as the initial architecture-evaluation model. The
`ArchitectureEvaluator` calls Ollama's local `/api/chat` HTTP endpoint and
requests a structured response matching `FileValidationResult`.

The default Ollama endpoint is `http://localhost:11434`. Ollama installation
and model download are environment prerequisites; the Python service uses its
existing `httpx` dependency and does not require the Python Ollama client.

## Consequences

* **Positive:** Keeps repository contents local, provides a
  code-oriented model, and avoids adding an orchestration framework for a
  single model call.
* **Negative/Risks:** Requires Ollama and the model to be installed and
  running locally. Evaluation speed and output quality depend on available
  CPU, GPU, and memory, and changing the model may require retuning prompts
  and validation tests.
