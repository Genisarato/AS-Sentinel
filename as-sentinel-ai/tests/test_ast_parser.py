import pytest

from app.services.ast_parser import CodeASTParser, parse_file, parse_source


def test_parse_source_extracts_classes_methods_and_imports():
    metadata = parse_source(
        """\
from app.infrastructure.db import Repository
from .domain import Entity
import requests as http

@dataclass
class Order(Entity):
    \"\"\"An order aggregate.\"\"\"

    @classmethod
    def create(cls):
        return cls()

    async def save(self):
        return await http.post("/")
""",
        filename="order.py",
    )

    assert metadata.filename == "order.py"
    assert [item.name for item in metadata.classes] == ["Order"]
    order = metadata.classes[0]
    assert order.bases == ("Entity",)
    assert order.decorators == ("dataclass",)
    assert order.docstring == "An order aggregate."
    assert [(method.name, method.is_async) for method in order.methods] == [
        ("create", False),
        ("save", True),
    ]
    assert order.methods[0].decorators == ("classmethod",)
    assert [item.module for item in metadata.imports] == [
        "app.infrastructure.db",
        "domain",
        "requests",
    ]
    assert metadata.imports[1].is_relative is True
    assert metadata.external_dependencies == (
        "app.infrastructure.db",
        "requests",
    )


def test_parse_source_preserves_import_details_and_declaration_locations():
    metadata = parse_source(
        "import os, sys as system\n"
        "from ...shared import one, two as alias\n"
        "\n"
        "class Example:\n"
        "    def method(self):\n"
        "        pass\n",
        filename="example.py",
    )

    assert metadata.imports[0].module == "os"
    assert metadata.imports[0].names == ("os",)
    assert metadata.imports[0].lineno == 1
    assert metadata.imports[1].module == "sys"
    assert metadata.imports[1].names == ("system",)
    assert metadata.imports[2].module == "shared"
    assert metadata.imports[2].names == ("one", "two")
    assert metadata.imports[2].level == 3
    assert metadata.imports[2].is_relative is True

    example = metadata.classes[0]
    assert example.lineno == 4
    assert example.end_lineno == 6
    assert example.methods[0].lineno == 5
    assert example.methods[0].end_lineno == 6


def test_code_ast_parser_returns_scanner_compatible_summary():
    summary = CodeASTParser(
        "import json\n\n"
        "class Example:\n"
        "    @staticmethod\n"
        "    def build():\n"
        "        return json.dumps({})\n",
        file_path="example.py",
    ).get_summary()

    assert summary == {
        "file_path": "example.py",
        "classes": [
            {
                "name": "Example",
                "lineno": 3,
                "end_lineno": 6,
                "bases": [],
                "decorators": [],
                "docstring": None,
                "methods": [
                    {
                        "name": "build",
                        "lineno": 5,
                        "end_lineno": 6,
                        "is_async": False,
                        "decorators": ["staticmethod"],
                    }
                ],
            }
        ],
        "imports": [
            {
                "module": "json",
                "names": ["json"],
                "lineno": 1,
                "level": 0,
                "is_relative": False,
            }
        ],
        "external_dependencies": ["json"],
    }


def test_parse_file_reads_python_source(tmp_path):
    source_path = tmp_path / "module.py"
    source_path.write_text("class Example:\n    pass\n", encoding="utf-8")

    metadata = parse_file(source_path)

    assert metadata.filename == str(source_path)
    assert metadata.classes[0].name == "Example"


def test_parse_file_rejects_non_python_files(tmp_path):
    source_path = tmp_path / "module.txt"
    source_path.write_text("class Example:\n    pass\n", encoding="utf-8")

    with pytest.raises(ValueError, match=r"\.py"):
        parse_file(source_path)


def test_parse_source_exposes_syntax_errors():
    with pytest.raises(SyntaxError, match="broken.py"):
        parse_source("class Broken(", filename="broken.py")
