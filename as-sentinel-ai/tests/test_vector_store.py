from types import SimpleNamespace
from unittest.mock import Mock

import pytest

from app.core.vector_store import QdrantVectorStore
from app.vector_store import qdrant as qdrant_module


@pytest.fixture(scope="module")
def vector_store():
    """Fixture providing initialized QdrantVectorStore instance."""
    store = QdrantVectorStore()
    store.init_collection()
    return store


@pytest.mark.integration
def test_vector_store_initialization(vector_store):
    """Ensure vector store connects and collection exists."""
    status = vector_store.init_collection()
    assert status == "already_exists"


@pytest.mark.integration
def test_upsert_and_semantic_search(vector_store):
    """Verify vector generation and similarity retrieval in Qdrant."""
    rule_id = "TEST-VECTOR-002"
    title = "Database Direct Access Ban"
    content = "Controllers must never invoke SQL queries directly; use repository abstractions."
    
    point_id = vector_store.upsert_rule(rule_id=rule_id, title=title, content=content)
    assert point_id is not None

    hits = vector_store.search_similar_rules(query="SQL in controllers", limit=1)
    assert len(hits) > 0
    assert hits[0]["rule_id"] == rule_id


def make_store(client=None, encoder=None):
    store = object.__new__(QdrantVectorStore)
    store.client = client or Mock()
    store.collection_name = "test-rules"
    store.encoder = encoder or Mock()
    return store


def test_init_collection_creates_missing_collection_with_config(monkeypatch):
    client = Mock()
    client.get_collections.return_value.collections = []
    store = make_store(client=client)

    result = store.init_collection()

    assert result == "created"
    client.create_collection.assert_called_once()
    request = client.create_collection.call_args.kwargs
    assert request["collection_name"] == "test-rules"
    assert request["vectors_config"].size == qdrant_module.settings.VECTOR_SIZE


def test_init_collection_is_idempotent_when_collection_exists():
    client = Mock()
    client.get_collections.return_value.collections = [
        SimpleNamespace(name="test-rules"),
    ]
    store = make_store(client=client)

    assert store.init_collection() == "already_exists"
    client.create_collection.assert_not_called()


def test_upsert_rule_encodes_content_and_stores_complete_payload():
    encoder = Mock()
    encoder.encode.return_value.tolist.return_value = [0.1, 0.2]
    client = Mock()
    store = make_store(client=client, encoder=encoder)

    point_id = store.upsert_rule(
        rule_id="RULE-001",
        title="Use repositories",
        content="Domain code must not access SQL directly.",
        category="HIGH",
        tags=["ddd", "architecture"],
    )

    assert point_id == str(qdrant_module.uuid.uuid5(qdrant_module.uuid.NAMESPACE_DNS, "RULE-001"))
    encoder.encode.assert_called_once_with("Domain code must not access SQL directly.")
    client.upsert.assert_called_once()
    point = client.upsert.call_args.kwargs["points"][0]
    assert point.id == point_id
    assert point.vector == [0.1, 0.2]
    assert point.payload == {
        "rule_id": "RULE-001",
        "title": "Use repositories",
        "content": "Domain code must not access SQL directly.",
        "category": "HIGH",
        "tags": ["ddd", "architecture"],
    }


def test_upsert_rule_uses_empty_tags_when_tags_are_omitted():
    encoder = Mock()
    encoder.encode.return_value.tolist.return_value = [0.5]
    client = Mock()
    store = make_store(client=client, encoder=encoder)

    store.upsert_rule("RULE-002", "Title", "Content")

    point = client.upsert.call_args.kwargs["points"][0]
    assert point.payload["tags"] == []


def test_search_maps_payload_and_score_and_forwards_limit():
    encoder = Mock()
    encoder.encode.return_value.tolist.return_value = [0.3, 0.4]
    client = Mock()
    client.query_points.return_value.points = [
        SimpleNamespace(
            payload={
                "rule_id": "RULE-001",
                "title": "Rule",
                "content": "Content",
                "category": "MEDIUM",
                "tags": ["ddd"],
            },
            score=0.91,
        ),
        SimpleNamespace(payload=None, score=0.12),
    ]
    store = make_store(client=client, encoder=encoder)

    results = store.search_similar_rules("architecture", limit=2)

    encoder.encode.assert_called_once_with("architecture")
    client.query_points.assert_called_once_with(
        collection_name="test-rules",
        query=[0.3, 0.4],
        limit=2,
        with_payload=True,
    )
    assert results == [
        {
            "rule_id": "RULE-001",
            "title": "Rule",
            "content": "Content",
            "category": "MEDIUM",
            "tags": ["ddd"],
            "score": 0.91,
        },
        {
            "rule_id": None,
            "title": None,
            "content": None,
            "category": None,
            "tags": [],
            "score": 0.12,
        },
    ]