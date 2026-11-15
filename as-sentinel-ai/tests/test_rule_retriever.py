from unittest.mock import MagicMock
from app.services.rule_retriever import ArchitectureRuleRetriever


def test_retrieve_relevant_rules():
    # Mock embedding model
    mock_model = MagicMock()
    mock_model.encode.return_value.tolist.return_value = [0.1, 0.2, 0.3]

    # Mock Qdrant response
    mock_point = MagicMock()
    mock_point.id = "adr-001"
    mock_point.score = 0.8543
    mock_point.payload = {
        "rule_id": "ADR-001",
        "title": "Domain Layer Isolation",
        "content": "Domain entities and services must not import infrastructure frameworks like FastAPI.",
        "severity": "error"
    }

    mock_response = MagicMock()
    mock_response.points = [mock_point]

    mock_qdrant = MagicMock()
    mock_qdrant.query_points.return_value = mock_response

    retriever = ArchitectureRuleRetriever(qdrant_client=mock_qdrant, embedding_model=mock_model)
    results = retriever.retrieve_relevant_rules("File path: app/domain/user.py\nDependencies/Imports: fastapi")

    assert len(results) == 1
    assert results[0]["rule_id"] == "ADR-001"
    assert results[0]["score"] == 0.8543


def test_retrieve_forwards_embedding_and_search_parameters():
    mock_model = MagicMock()
    mock_model.encode.return_value.tolist.return_value = [0.4, 0.5]
    mock_qdrant = MagicMock()
    mock_qdrant.query_points.return_value.points = []

    retriever = ArchitectureRuleRetriever(
        qdrant_client=mock_qdrant,
        embedding_model=mock_model,
        collection_name="custom-rules",
    )

    results = retriever.retrieve_relevant_rules(
        "File path: app/api/users.py",
        top_k=10,
        score_threshold=0.8,
    )

    mock_model.encode.assert_called_once_with("File path: app/api/users.py")
    mock_qdrant.query_points.assert_called_once_with(
        collection_name="custom-rules",
        query=[0.4, 0.5],
        limit=10,
        score_threshold=0.8,
        with_payload=True,
    )
    assert results == []


def test_retrieve_uses_safe_defaults_for_incomplete_payloads():
    mock_model = MagicMock()
    mock_model.encode.return_value.tolist.return_value = [0.1]

    category_point = MagicMock(
        id="rule-001",
        score=0.75678,
        payload={"category": "HIGH"},
    )
    empty_point = MagicMock(id="rule-002", score=0.4, payload=None)

    mock_qdrant = MagicMock()
    mock_qdrant.query_points.return_value.points = [
        category_point,
        empty_point,
    ]

    retriever = ArchitectureRuleRetriever(
        qdrant_client=mock_qdrant,
        embedding_model=mock_model,
    )

    results = retriever.retrieve_relevant_rules("architecture")

    assert results == [
        {
            "rule_id": "rule-001",
            "title": "Untitled Rule",
            "content": "",
            "severity": "HIGH",
            "score": 0.7568,
        },
        {
            "rule_id": "rule-002",
            "title": "Untitled Rule",
            "content": "",
            "severity": "warning",
            "score": 0.4,
        },
    ]