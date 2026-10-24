import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_read_root():
    """
    Test the root endpoint status response.
    """
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"service": "as-sentinel-ai", "status": "running"}


def test_qdrant_healthcheck():
    """
    Test Qdrant healthcheck endpoint connectivity.
    """
    response = client.get("/api/v1/rules/health/qdrant")
    assert response.status_code == 200
    data = response.json()
    assert "qdrant_status" in data
    assert data["qdrant_status"] == "connected"


def test_init_rules_collection():
    """
    Test initialization of Qdrant vector store collection.
    """
    response = client.post("/api/v1/rules/init-collection")
    assert response.status_code == 201
    data = response.json()
    assert data["collection"] == "architecture_rules"
    assert data["status"] in ["created", "already_exists"]