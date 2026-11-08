import os

from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)
ENDPOINT = "/api/v1/ast/structure"


def test_scan_structure_returns_complete_file_summary(tmp_path):
    source_path = tmp_path / "controller.py"
    source_path.write_text(
        "from fastapi import APIRouter\n"
        "import requests\n\n"
        "class UserController:\n"
        "    def get_users(self):\n"
        "        pass\n",
        encoding="utf-8",
    )
    (tmp_path / "README.md").write_text("not Python", encoding="utf-8")

    response = client.post(ENDPOINT, json={"directory_path": str(tmp_path)})

    assert response.status_code == 200
    assert response.json() == {
        "total_files": 1,
        "results": [
            {
                "file_path": "controller.py",
                "imports": [
                    {
                        "module": "fastapi",
                        "names": ["APIRouter"],
                        "lineno": 1,
                        "level": 0,
                        "is_relative": False,
                    },
                    {
                        "module": "requests",
                        "names": ["requests"],
                        "lineno": 2,
                        "level": 0,
                        "is_relative": False,
                    },
                ],
                "classes": [
                    {
                        "name": "UserController",
                        "lineno": 4,
                        "end_lineno": 6,
                        "bases": [],
                        "decorators": [],
                        "docstring": None,
                        "methods": [
                            {
                                "name": "get_users",
                                "lineno": 5,
                                "end_lineno": 6,
                                "is_async": False,
                                "decorators": [],
                            }
                        ],
                    }
                ],
                "external_dependencies": ["fastapi", "requests"],
                "error": None,
            }
        ],
    }


def test_scan_structure_includes_nested_files_and_ignores_configured_directories(tmp_path):
    nested = tmp_path / "domain"
    nested.mkdir()
    (nested / "model.py").write_text("class Model: pass\n", encoding="utf-8")
    ignored = tmp_path / ".venv"
    ignored.mkdir()
    (ignored / "third_party.py").write_text("class Ignored: pass\n", encoding="utf-8")

    response = client.post(ENDPOINT, json={"directory_path": str(tmp_path)})

    assert response.status_code == 200
    assert response.json()["total_files"] == 1
    assert response.json()["results"][0]["file_path"] == os.path.join("domain", "model.py")


def test_scan_structure_reports_invalid_python_and_continues(tmp_path):
    (tmp_path / "broken.py").write_text("class Broken(", encoding="utf-8")
    (tmp_path / "valid.py").write_text("class Valid: pass\n", encoding="utf-8")

    response = client.post(ENDPOINT, json={"directory_path": str(tmp_path)})

    assert response.status_code == 200
    results = {item["file_path"]: item for item in response.json()["results"]}
    assert "broken.py" in results
    assert "broken.py" in results["broken.py"]["error"]
    assert "line 1" in results["broken.py"]["error"]
    assert results["valid.py"]["classes"][0]["name"] == "Valid"


def test_scan_structure_returns_not_found_for_missing_directory(tmp_path):
    missing = tmp_path / "does-not-exist"

    response = client.post(ENDPOINT, json={"directory_path": str(missing)})

    assert response.status_code == 404
    assert response.json()["detail"] == f"Directory '{missing}' not found."


def test_scan_structure_validates_directory_path():
    response = client.post(ENDPOINT, json={})

    assert response.status_code == 422
    assert response.json()["detail"][0]["loc"] == ["body", "directory_path"]
