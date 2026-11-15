import json
import pytest
from unittest.mock import patch, MagicMock
from app.services.architecture_evaluator import ArchitectureEvaluator


def test_evaluate_file_no_rules_returns_compliant():
    evaluator = ArchitectureEvaluator()
    result = evaluator.evaluate_file(
        file_path="app/domain/user.py",
        file_context="File path: app/domain/user.py\nDependencies/Imports: None",
        retrieved_rules=[]
    )
    assert result.is_compliant is True
    assert result.violations == []


@patch("app.services.architecture_evaluator.httpx.Client")
def test_evaluate_file_detects_violation(mock_client_cls):
    mock_llm_output = {
        "file_path": "app/domain/user_service.py",
        "is_compliant": False,
        "violations": [
            {
                "rule_id": "ADR-001",
                "severity": "error",
                "reason": "Domain layer imports FastAPI APIRouter directly.",
                "suggestion": "Move router definitions to app/api/ and keep domain pure."
            }
        ]
    }

    mock_response = MagicMock()
    mock_response.json.return_value = {
        "message": {"content": json.dumps(mock_llm_output)}
    }
    mock_client_instance = MagicMock()
    mock_client_instance.post.return_value = mock_response
    mock_client_cls.return_value.__enter__.return_value = mock_client_instance

    evaluator = ArchitectureEvaluator(model_name="qwen2.5-coder:7b")
    rules = [{
        "rule_id": "ADR-001",
        "title": "Domain Isolation",
        "content": "Domain must not import web frameworks.",
        "severity": "error"
    }]

    result = evaluator.evaluate_file(
        file_path="app/domain/user_service.py",
        file_context="File path: app/domain/user_service.py\nDependencies/Imports: fastapi (APIRouter)",
        retrieved_rules=rules
    )

    assert result.is_compliant is False
    assert len(result.violations) == 1
    assert result.violations[0].rule_id == "ADR-001"


@patch("app.services.architecture_evaluator.httpx.Client")
def test_evaluate_file_returns_compliant_result_from_empty_violation_list(
    mock_client_cls,
):
    mock_response = MagicMock()
    mock_response.json.return_value = {
        "message": {
            "content": json.dumps(
                {
                    "file_path": "app/domain/user.py",
                    "is_compliant": True,
                    "violations": [],
                }
            )
        }
    }
    mock_client = MagicMock()
    mock_client.post.return_value = mock_response
    mock_client_cls.return_value.__enter__.return_value = mock_client

    evaluator = ArchitectureEvaluator(
        ollama_base_url="http://ollama.test/",
        model_name="test-model",
    )
    result = evaluator.evaluate_file(
        file_path="app/domain/user.py",
        file_context="File path: app/domain/user.py\nDependencies/Imports: None",
        retrieved_rules=[
            {
                "rule_id": "ADR-001",
                "title": "Domain Isolation",
                "content": "Keep the domain independent.",
                "severity": "warning",
            }
        ],
    )

    assert result.file_path == "app/domain/user.py"
    assert result.is_compliant is True
    assert result.violations == []


@patch("app.services.architecture_evaluator.httpx.Client")
def test_evaluate_file_builds_expected_ollama_request(mock_client_cls):
    mock_response = MagicMock()
    mock_response.json.return_value = {
        "message": {
            "content": json.dumps(
                {
                    "file_path": "ignored-by-service",
                    "is_compliant": False,
                    "violations": [
                        {
                            "rule_id": "ADR-002",
                            "severity": "warning",
                            "reason": "Infrastructure dependency found.",
                            "suggestion": "Move the dependency behind an adapter.",
                        }
                    ],
                }
            )
        }
    }
    mock_client = MagicMock()
    mock_client.post.return_value = mock_response
    mock_client_cls.return_value.__enter__.return_value = mock_client

    evaluator = ArchitectureEvaluator(model_name="test-model")
    file_context = "File path: app/domain/order.py\nDependencies/Imports: sqlalchemy"
    rules = [
        {
            "rule_id": "ADR-002",
            "title": "Persistence Isolation",
            "content": "Domain code must not import persistence frameworks.",
            "severity": "warning",
        }
    ]

    result = evaluator.evaluate_file("app/domain/order.py", file_context, rules)

    request = mock_client.post.call_args
    assert request.args == ("http://localhost:11434/api/chat",)
    payload = request.kwargs["json"]
    assert payload["model"] == "test-model"
    assert payload["stream"] is False
    assert payload["options"] == {"temperature": 0.0}
    assert payload["format"] == result.model_json_schema()
    assert payload["messages"][0]["role"] == "system"
    assert file_context in payload["messages"][1]["content"]
    assert "[ADR-002] Persistence Isolation (Severity: warning)" in payload["messages"][1]["content"]
    assert result.file_path == "app/domain/order.py"
    assert result.is_compliant is False


@patch("app.services.architecture_evaluator.httpx.Client")
def test_evaluate_file_propagates_ollama_http_errors(mock_client_cls):
    mock_client = MagicMock()
    mock_client.post.side_effect = RuntimeError("Ollama is unavailable")
    mock_client_cls.return_value.__enter__.return_value = mock_client

    evaluator = ArchitectureEvaluator()

    with pytest.raises(RuntimeError, match="Ollama is unavailable"):
        evaluator.evaluate_file(
            file_path="app/domain/user.py",
            file_context="File path: app/domain/user.py",
            retrieved_rules=[
                {
                    "rule_id": "ADR-001",
                    "title": "Rule",
                    "content": "Content",
                    "severity": "error",
                }
            ],
        )


@patch("app.services.architecture_evaluator.httpx.Client")
def test_evaluate_file_reports_incomplete_ollama_response(mock_client_cls):
    mock_response = MagicMock()
    mock_response.json.return_value = {"message": {}}
    mock_client = MagicMock()
    mock_client.post.return_value = mock_response
    mock_client_cls.return_value.__enter__.return_value = mock_client

    evaluator = ArchitectureEvaluator()

    with pytest.raises(RuntimeError, match="incomplete or invalid response"):
        evaluator.evaluate_file(
            file_path="app/domain/user.py",
            file_context="File path: app/domain/user.py",
            retrieved_rules=[{"rule_id": "ADR-001", "title": "Rule", "content": "Content", "severity": "error"}],
        )


@patch("app.services.architecture_evaluator.httpx.Client")
def test_evaluate_file_reports_invalid_json_content(mock_client_cls):
    mock_response = MagicMock()
    mock_response.json.return_value = {"message": {"content": "not-json"}}
    mock_client = MagicMock()
    mock_client.post.return_value = mock_response
    mock_client_cls.return_value.__enter__.return_value = mock_client

    evaluator = ArchitectureEvaluator()

    with pytest.raises(RuntimeError, match="invalid JSON"):
        evaluator.evaluate_file(
            file_path="app/domain/user.py",
            file_context="File path: app/domain/user.py",
            retrieved_rules=[{"rule_id": "ADR-001", "title": "Rule", "content": "Content", "severity": "error"}],
        )