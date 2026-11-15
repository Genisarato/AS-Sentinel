from unittest.mock import Mock

from fastapi.testclient import TestClient

from app.api.v1.endpoints.audit import get_evaluator, get_rule_retriever
from app.main import app
from app.schemas.validation import FileValidationResult


client = TestClient(app)


def test_audit_returns_not_found_for_missing_directory(tmp_path):
    missing = tmp_path / "does-not-exist"

    app.dependency_overrides[get_rule_retriever] = lambda: Mock()
    app.dependency_overrides[get_evaluator] = lambda: Mock()
    try:
        response = client.post(
            "/api/v1/audit/",
            json={"directory_path": str(missing)},
        )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 404
    assert response.json()["detail"] == f"Directory '{missing}' not found."


def test_audit_runs_retrieval_and_evaluation_for_scanned_files(tmp_path):
    source_path = tmp_path / "user.py"
    source_path.write_text(
        "from fastapi import APIRouter\n\n"
        "class UserController:\n"
        "    def get_user(self):\n"
        "        pass\n",
        encoding="utf-8",
    )

    retriever = Mock()
    retriever.retrieve_relevant_rules.return_value = [
        {
            "rule_id": "ADR-001",
            "title": "Domain isolation",
            "content": "Domain code must not import web frameworks.",
            "severity": "HIGH",
        }
    ]
    evaluator = Mock()
    evaluator.evaluate_file.return_value = FileValidationResult(
        file_path="user.py",
        is_compliant=False,
        violations=[
            {
                "rule_id": "ADR-001",
                "severity": "HIGH",
                "reason": "The file imports FastAPI.",
                "suggestion": "Move the router to the API layer.",
            }
        ],
    )

    app.dependency_overrides[get_rule_retriever] = lambda: retriever
    app.dependency_overrides[get_evaluator] = lambda: evaluator
    try:
        response = client.post(
            "/api/v1/audit/",
            json={
                "directory_path": str(tmp_path),
                "top_k_rules": 5,
                "score_threshold": 0.6,
            },
        )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json() == {
        "directory_path": str(tmp_path),
        "total_files_scanned": 1,
        "total_violations": 1,
        "is_project_compliant": False,
        "results": [
            {
                "file_path": "user.py",
                "is_compliant": False,
                "violations": [
                    {
                        "rule_id": "ADR-001",
                        "severity": "HIGH",
                        "reason": "The file imports FastAPI.",
                        "suggestion": "Move the router to the API layer.",
                    }
                ],
            }
        ],
    }
    retriever.retrieve_relevant_rules.assert_called_once()
    assert retriever.retrieve_relevant_rules.call_args.kwargs["top_k"] == 5
    assert retriever.retrieve_relevant_rules.call_args.kwargs["score_threshold"] == 0.6
    evaluator.evaluate_file.assert_called_once()
    assert evaluator.evaluate_file.call_args.kwargs["file_path"] == "user.py"


def test_audit_skips_files_with_scan_errors(tmp_path):
    (tmp_path / "broken.py").write_text("class Broken(", encoding="utf-8")

    retriever = Mock()
    evaluator = Mock()
    app.dependency_overrides[get_rule_retriever] = lambda: retriever
    app.dependency_overrides[get_evaluator] = lambda: evaluator
    try:
        response = client.post(
            "/api/v1/audit/",
            json={"directory_path": str(tmp_path)},
        )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json() == {
        "directory_path": str(tmp_path),
        "total_files_scanned": 0,
        "total_violations": 0,
        "is_project_compliant": True,
        "results": [],
    }
    retriever.retrieve_relevant_rules.assert_not_called()
    evaluator.evaluate_file.assert_not_called()
