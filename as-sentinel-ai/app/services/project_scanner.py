import os
from typing import List, Dict, Any
from app.services.ast_parser import CodeASTParser

class ProjectScanner:
    """
    Scans a directory recursively to parse all Python files and aggregate their AST structures.
    """

    def __init__(self, root_dir: str, ignore_dirs: List[str] = None):
        self.root_dir = root_dir
        self.ignore_dirs = ignore_dirs or [".venv", "venv", "__pycache__", ".git", ".pytest_cache", "build", "dist"]

    def scan(self) -> List[Dict[str, Any]]:
        """Walks through root_dir, parses Python files, and returns structural summaries."""
        results = []
        for root, dirs, files in os.walk(self.root_dir):
            # Filtering ignored directories in-place
            dirs[:] = [d for d in dirs if d not in self.ignore_dirs]

            for file in files:
                if file.endswith(".py"):
                    full_path = os.path.join(root, file)
                    rel_path = os.path.relpath(full_path, self.root_dir)
                    try:
                        with open(full_path, "r", encoding="utf-8") as f:
                            code_content = f.read()
                        
                        parser = CodeASTParser(code_content, file_path=rel_path)
                        results.append(parser.get_summary())
                    except Exception as e:
                        # Handles syntax errors or unreadable files gracefully
                        results.append({
                            "file_path": rel_path,
                            "error": str(e)
                        })
        return results