from app.services.ast_parser import CodeASTParser
from app.services.context_builder import ASTContextBuilder


def test_build_file_context():
    sample_code = """
from fastapi import APIRouter
from app.infrastructure.db import PostgresSession

class UserDomainService:
    def create_user(self):
        pass
"""
    summary = CodeASTParser(sample_code, file_path="app/domain/user_service.py").get_summary()
    context_text = ASTContextBuilder.build_file_context(summary)

    assert "File path: app/domain/user_service.py" in context_text
    assert "fastapi (APIRouter)" in context_text
    assert "app.infrastructure.db (PostgresSession)" in context_text
    assert "Class 'UserDomainService' defines methods: [create_user]" in context_text


def test_build_file_context_handles_empty_and_relative_imports():
    context_text = ASTContextBuilder.build_file_context(
        {
            "file_path": "app/domain/entity.py",
            "imports": [
                {"module": "shared", "names": ["Entity"], "level": 2},
                {"module": "", "names": []},
            ],
            "classes": [],
        }
    )

    assert "File path: app/domain/entity.py" in context_text
    assert "Dependencies/Imports: ..shared (Entity)" in context_text
    assert "Classes defined: None" in context_text


def test_build_file_context_handles_string_and_incomplete_members():
    context_text = ASTContextBuilder.build_file_context(
        {
            "file_path": "module.py",
            "imports": [{"module": "logging", "names": "getLogger"}],
            "classes": [
                "LegacyService",
                {
                    "bases": ["BaseService", {"name": "Auditable"}],
                    "methods": ["run", {"name": "stop"}],
                },
            ],
        }
    )

    assert "Dependencies/Imports: logging (getLogger)" in context_text
    assert "Class 'LegacyService' defines methods: [no methods]" in context_text
    assert "Class 'Unknown' inherits from (BaseService, Auditable) defines methods: [run, stop]" in context_text