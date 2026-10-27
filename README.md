# AS-Sentinel (Architecture & Security Sentinel)

> **Work in progress:** This project is under active development. Its architecture, APIs, and features may change.

Distributed platform for AI-powered architecture and security auditing of codebases and specifications.

## Architecture

- **Core service (`as-sentinel-core`):** Java 21 and Spring Boot 4.1.1. Currently exposes a REST health endpoint and is configured to use PostgreSQL.
- **AI service (`as-sentinel-ai`):** Python, FastAPI, and the Qdrant client. Provides service health checks and architecture-rule vector-store operations.
- **Vector store:** Qdrant, using the `architecture_rules` collection with 384-dimensional cosine-distance vectors.
- **Relational database:** PostgreSQL 16.

## Architecture Decision Records

Architecture Decision Records (ADRs) document the project's important architectural choices, their context, and their trade-offs. They help keep the implementation aligned with the intended design as the project evolves.

The ADRs are maintained in [`docs/adr/`](docs/adr/). See the [ADR index](docs/adr/README.md) for the complete list. Each record is marked **Accepted** when the decision is reflected in the current design or code, or **Proposed** when it describes planned architecture that has not yet been implemented.

## Prerequisites

- Docker and Docker Compose
- Python 3.10+
- Java 21

## Quickstart

### 1. Start the infrastructure

From the repository root, start Qdrant and PostgreSQL:

```bash
docker compose up -d
```

The services are available at Qdrant's REST API on `http://localhost:6333`, its gRPC API on `localhost:6334`, and PostgreSQL on `localhost:5432`. The Compose file configures a local development database (`assentinel`) with the `postgres` user and `postgrespassword` password; do not use these credentials in production.

### 2. Run the AI service

From `as-sentinel-ai`, create and activate a virtual environment, then install dependencies:

```bash
python -m venv .venv
```

On Windows PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
```

On macOS/Linux:

```bash
source .venv/bin/activate
```

Then, from `as-sentinel-ai`, install dependencies and start the API:

```bash
python -m pip install -r requirements.txt
uvicorn app.main:app --reload
```

The service listens on `http://localhost:8000`. It connects to Qdrant at `localhost:6333` by default. Override the connection with the `QDRANT_HOST` and `QDRANT_PORT` environment variables.

### 3. Run the core service

With PostgreSQL running, start the Spring Boot application from `as-sentinel-core`:

```bash
./mvnw spring-boot:run
```

On Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

The core service listens on `http://localhost:8080`.

## API endpoints

### AI service

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/` | Checks that the AI service is running. |
| `GET` | `/api/v1/rules/health/qdrant` | Checks the Qdrant connection and lists existing collections. |
| `POST` | `/api/v1/rules/init-collection` | Creates the default architecture-rules collection if it does not exist. |

Interactive API documentation is available at `http://localhost:8000/docs`.

### Core service

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/health` | Returns the core service health status. |

The core service uses its HTTP client to consume the AI rules API. The base URL
defaults to `http://localhost:8000` and can be overridden with the
`AS_SENTINEL_AI_BASE_URL` environment variable (or the
`as-sentinel.ai.base-url` Spring property). The client currently supports:

| Method | Path | Client operation |
| --- | --- | --- |
| `GET` | `/api/v1/rules/health/qdrant` | Reads Qdrant connectivity and existing collections. |
| `POST` | `/api/v1/rules/init-collection` | Ensures the architecture-rules collection exists. |

## Tests

Run the AI service tests from `as-sentinel-ai`:

```bash
python -m pytest
```

Run the core service tests from `as-sentinel-core`:

```bash
./mvnw test
```

On Windows, use `.\mvnw.cmd test`.
