from unittest.mock import Mock

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.api.v1 import rules

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


def test_ingest_accepts_all_fields_and_forwards_them(monkeypatch):
    mocked_upsert = lambda **kwargs: "point-123"
    monkeypatch.setattr(rules.vector_store, "upsert_rule", mocked_upsert)

    response = client.post(
        "/api/v1/rules/ingest",
        json={
            "rule_id": "RULE-123",
            "title": "Repository boundary",
            "description": "Use a repository abstraction.",
            "severity": "HIGH",
            "tags": ["ddd", "architecture"],
        },
    )

    assert response.status_code == 201
    assert response.json() == {
        "status": "indexed",
        "point_id": "point-123",
        "rule_id": "RULE-123",
    }


def test_ingest_returns_server_error_when_vector_store_fails(monkeypatch):
    def fail(**kwargs):
        raise RuntimeError("qdrant unavailable")

    monkeypatch.setattr(rules.vector_store, "upsert_rule", fail)

    response = client.post(
        "/api/v1/rules/ingest",
        json={
            "rule_id": "RULE-FAIL",
            "title": "Rule",
            "description": "Description",
        },
    )

    assert response.status_code == 500
    assert response.json()["detail"] == "Failed to ingest rule 'RULE-FAIL' into Qdrant."


def test_search_forwards_query_and_limit_and_returns_store_results(monkeypatch):
    search = Mock(return_value=[{
        "rule_id": "RULE-001",
        "title": "Rule",
        "content": "Content",
        "category": "LOW",
        "tags": [],
        "score": 0.8,
    }])
    monkeypatch.setattr(rules.vector_store, "search_similar_rules", search)

    response = client.post(
        "/api/v1/rules/search",
        json={"query": "repositories", "limit": 1},
    )

    assert response.status_code == 200
    assert response.json()["results"][0]["rule_id"] == "RULE-001"
    search.assert_called_once_with(query="repositories", limit=1)


@pytest.mark.parametrize(
    ("payload", "field"),
    [
        ({"title": "Rule", "description": "Description"}, "rule_id"),
        ({"rule_id": "RULE", "description": "Description"}, "title"),
        ({"rule_id": "RULE", "title": "Rule"}, "description"),
    ],
)
def test_ingest_requires_all_required_fields(payload, field):
    response = client.post("/api/v1/rules/ingest", json=payload)

    assert response.status_code == 422
    assert any(error["loc"][-1] == field for error in response.json()["detail"])
