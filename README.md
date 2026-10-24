# AS-Sentinel (Architecture & Security Sentinel)

> ⚠️ **Work in Progress (WIP):** This project is currently under active development. The architecture, APIs, and features are subject to rapid changes.

Distributed AI-powered architecture & security auditing platform for codebases and specs.

## Architecture

- **Core Service (`as-sentinel-core`):** Java 21 / Spring Boot 3 (Clean Architecture / DDD)
- **AI Engine (`as-sentinel-ai`):** Python / FastAPI & Qdrant Integration
- **Vector Store:** Qdrant DB
- **Database:** PostgreSQL 16

---

## Quickstart

### 1. Start Infrastructure with Docker

Launch the vector database (Qdrant) and relational database (PostgreSQL):

```bash
docker compose up -d