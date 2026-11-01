import pytest
from app.core.vector_store import QdrantVectorStore


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