# AS-Sentinel (Architecture & Security Sentinel)

> ⚠️ **Work in Progress (WIP):** This project is currently under active development. The architecture, APIs, and features are subject to rapid changes.

Distributed AI-powered architecture & security auditing platform for codebases and specs.

## Architecture

- **Core Service:** Java 21 / Spring Boot 3 (Clean Architecture / DDD)
- **AI Engine:** Python 3.15 / FastAPI (RAG, LlamaIndex)
- **Vector Store:** Qdrant DB
- **Database:** PostgreSQL 16

## Quickstart

1. Clone the repository
2. Start infrastructure with Docker:
   ```bash
   docker compose up -d