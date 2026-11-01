import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_read_root():
    """Test the root endpoint status response."""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"service": "as-sentinel-ai", "status": "running"}


@pytest.mark.integration
def test_qdrant_healthcheck():
    """Test Qdrant healthcheck endpoint connectivity."""
    response = client.get("/api/v1/rules/health/qdrant")
    assert response.status_code == 200
    data = response.json()
    assert "qdrant_status" in data
    assert data["qdrant_status"] == "connected"


@pytest.mark.integration
def test_init_rules_collection():
    """Test initialization of Qdrant vector store collection."""
    response = client.post("/api/v1/rules/init-collection")
    assert response.status_code == 201
    data = response.json()
    assert data["collection"] == "architecture_rules"
    assert data["status"] in ["created", "already_exists"]


@pytest.mark.integration
def test_ingest_and_search_rule():
    """Test rule ingestion and semantic search endpoints end-to-end."""
    # 1. Ingest a test rule
    ingest_payload = {
        "rule_id": "TEST-001",
        "title": "Hexagonal Architecture Isolation",
        "description": "Domain layer must remain strictly decoupled from external infrastructure frameworks.",
        "severity": "MEDIUM",
    }
    ingest_res = client.post("/api/v1/rules/ingest", json=ingest_payload)
    assert ingest_res.status_code == 201
    assert "point_id" in ingest_res.json()

    # 2. Search semantically for the ingested rule
    search_payload = {
        "query": "How should domain layer be decoupled?",
        "limit": 1
    }
    search_res = client.post("/api/v1/rules/search", json=search_payload)
    assert search_res.status_code == 200

    results = search_res.json().get("results", [])
    assert len(results) > 0
    assert results[0]["rule_id"] == "TEST-001"


def test_search_rejects_invalid_limit():
    response = client.post(
        "/api/v1/rules/search",
        json={"query": "architecture", "limit": 0},
    )

    assert response.status_code == 422


def test_ingest_rejects_empty_description():
    response = client.post(
        "/api/v1/rules/ingest",
        json={
            "rule_id": "TEST-INVALID",
            "title": "Invalid rule",
            "description": "",
        },
    )

    assert response.status_code == 422
