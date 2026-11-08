import os

from app.services.project_scanner import ProjectScanner


def test_project_scanner_scans_nested_python_files_and_ignores_non_python(tmp_path):
    (tmp_path / "service.py").write_text(
        "import sys\nclass Service:\n    pass\n",
        encoding="utf-8",
    )
    nested = tmp_path / "domain"
    nested.mkdir()
    (nested / "model.py").write_text(
        "class Model:\n    def save(self):\n        pass\n",
        encoding="utf-8",
    )
    (tmp_path / "README.md").write_text("# not Python", encoding="utf-8")

    results = ProjectScanner(str(tmp_path)).scan()
    by_path = {result["file_path"]: result for result in results}

    assert set(by_path) == {"service.py", os.path.join("domain", "model.py")}
    assert by_path["service.py"]["classes"][0]["name"] == "Service"
    assert by_path[os.path.join("domain", "model.py")]["classes"][0]["methods"] == [
        {
            "name": "save",
            "lineno": 2,
            "end_lineno": 3,
            "is_async": False,
            "decorators": [],
        }
    ]


def test_project_scanner_ignores_configured_directories(tmp_path):
    ignored = tmp_path / ".venv"
    ignored.mkdir()
    (ignored / "third_party.py").write_text("class Ignored: pass", encoding="utf-8")
    visible = tmp_path / "visible.py"
    visible.write_text("class Visible: pass", encoding="utf-8")

    results = ProjectScanner(str(tmp_path)).scan()

    assert [result["file_path"] for result in results] == ["visible.py"]


def test_project_scanner_reports_invalid_python_without_stopping(tmp_path):
    (tmp_path / "broken.py").write_text("class Broken(", encoding="utf-8")
    (tmp_path / "valid.py").write_text("class Valid: pass", encoding="utf-8")

    results = ProjectScanner(str(tmp_path)).scan()
    by_path = {result["file_path"]: result for result in results}

    assert "error" in by_path["broken.py"]
    assert "broken.py" in by_path["broken.py"]["error"]
    assert by_path["valid.py"]["classes"][0]["name"] == "Valid"