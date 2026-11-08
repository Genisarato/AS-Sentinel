# ADR-0011: Extract Python Architecture Metadata with the Native AST

* **Status:** Accepted
* **Date:** 2026-11-08
* **Deciders:** Project maintainers

## Context & Problem Statement

The AI service needs a deterministic first pass over Python repositories before
architecture and security rules can be applied. The analysis must identify
classes, methods, imports, and external dependencies without executing
repository code.

## Decision

Use Python's standard-library `ast` module as the parser for Python source
files. Expose the resulting structural analysis through the AI service at
`POST /api/v1/ast/structure`.

The implementation is split into:

* `app/services/ast_parser.py` for AST parsing and metadata extraction.
* `app/services/project_scanner.py` for recursive `.py` file discovery and
  ignored-directory handling.
* `app/schemas/ast.py` for the Pydantic request and response contract.

The scanner reports syntax or file errors per file and continues processing the
remaining project files. Repository code is parsed, not imported or executed.

## Consequences

* **Positive:** Deterministic analysis without executing untrusted source,
  no parser dependency, and source locations are available for later findings.
* **Negative/Risks:** The first version is Python-specific and reports syntax
  structure rather than runtime behavior. Large repositories still require
  limits, authorization, and resource controls before production exposure.

