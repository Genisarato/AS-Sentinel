"""Extract architectural information from Python source files."""

from __future__ import annotations

import ast
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class FunctionMetadata:
    """Metadata for a function or method declaration."""

    name: str
    lineno: int
    end_lineno: int | None
    is_async: bool
    decorators: tuple[str, ...]


@dataclass(frozen=True)
class ClassMetadata:
    """Metadata for a class declaration and its methods."""

    name: str
    lineno: int
    end_lineno: int | None
    bases: tuple[str, ...]
    decorators: tuple[str, ...]
    docstring: str | None
    methods: tuple[FunctionMetadata, ...]


@dataclass(frozen=True)
class ImportMetadata:
    """Metadata for an import statement."""

    module: str
    names: tuple[str, ...]
    lineno: int
    level: int

    @property
    def is_relative(self) -> bool:
        """Return whether the import uses one or more leading dots."""
        return self.level > 0


@dataclass(frozen=True)
class ModuleMetadata:
    """Architectural elements extracted from one Python module."""

    filename: str
    classes: tuple[ClassMetadata, ...]
    imports: tuple[ImportMetadata, ...]

    @property
    def external_dependencies(self) -> tuple[str, ...]:
        """Return unique absolute import modules in source order."""
        dependencies: list[str] = []
        for imported in self.imports:
            if imported.is_relative or imported.module in dependencies:
                continue
            dependencies.append(imported.module)
        return tuple(dependencies)


class CodeASTParser:
    """Compatibility facade for callers that parse source through an object."""

    def __init__(self, source: str, *, file_path: str = "<unknown>") -> None:
        self._metadata = parse_source(source, filename=file_path)

    def get_summary(self) -> dict[str, object]:
        """Return parsed metadata in a dictionary-oriented representation."""
        return {
            "file_path": self._metadata.filename,
            "classes": [
                {
                    "name": class_metadata.name,
                    "lineno": class_metadata.lineno,
                    "end_lineno": class_metadata.end_lineno,
                    "bases": list(class_metadata.bases),
                    "decorators": list(class_metadata.decorators),
                    "docstring": class_metadata.docstring,
                    "methods": [
                        {
                            "name": method.name,
                            "lineno": method.lineno,
                            "end_lineno": method.end_lineno,
                            "is_async": method.is_async,
                            "decorators": list(method.decorators),
                        }
                        for method in class_metadata.methods
                    ],
                }
                for class_metadata in self._metadata.classes
            ],
            "imports": [
                {
                    "module": imported.module,
                    "names": list(imported.names),
                    "lineno": imported.lineno,
                    "level": imported.level,
                    "is_relative": imported.is_relative,
                }
                for imported in self._metadata.imports
            ],
            "external_dependencies": list(self._metadata.external_dependencies),
        }


class _ArchitectureVisitor(ast.NodeVisitor):
    def __init__(self) -> None:
        self.classes: list[ClassMetadata] = []
        self.imports: list[ImportMetadata] = []

    def visit_ClassDef(self, node: ast.ClassDef) -> None:
        methods = tuple(
            _function_metadata(child)
            for child in node.body
            if isinstance(child, (ast.FunctionDef, ast.AsyncFunctionDef))
        )
        self.classes.append(
            ClassMetadata(
                name=node.name,
                lineno=node.lineno,
                end_lineno=getattr(node, "end_lineno", None),
                bases=tuple(_expression_name(base) for base in node.bases),
                decorators=tuple(
                    _expression_name(decorator) for decorator in node.decorator_list
                ),
                docstring=ast.get_docstring(node),
                methods=methods,
            )
        )
        self.generic_visit(node)

    def visit_Import(self, node: ast.Import) -> None:
        for alias in node.names:
            self.imports.append(
                ImportMetadata(
                    module=alias.name,
                    names=(alias.asname or alias.name.split(".")[0],),
                    lineno=node.lineno,
                    level=0,
                )
            )

    def visit_ImportFrom(self, node: ast.ImportFrom) -> None:
        module = node.module or ""
        self.imports.append(
            ImportMetadata(
                module=module,
                names=tuple(alias.name for alias in node.names),
                lineno=node.lineno,
                level=node.level,
            )
        )


def parse_source(source: str, *, filename: str = "<unknown>") -> ModuleMetadata:
    """Parse Python source and return its architectural metadata.

    ``SyntaxError`` is deliberately allowed to propagate so callers can report
    the exact source location of invalid Python.
    """
    tree = ast.parse(source, filename=filename)
    visitor = _ArchitectureVisitor()
    visitor.visit(tree)
    return ModuleMetadata(
        filename=filename,
        classes=tuple(visitor.classes),
        imports=tuple(visitor.imports),
    )


def parse_file(path: str | Path) -> ModuleMetadata:
    """Read and parse a Python source file."""
    source_path = Path(path)
    if source_path.suffix.lower() != ".py":
        raise ValueError(f"Expected a Python file (.py), got: {source_path}")
    return parse_source(
        source_path.read_text(encoding="utf-8"),
        filename=str(source_path),
    )


def _function_metadata(node: ast.FunctionDef | ast.AsyncFunctionDef) -> FunctionMetadata:
    return FunctionMetadata(
        name=node.name,
        lineno=node.lineno,
        end_lineno=getattr(node, "end_lineno", None),
        is_async=isinstance(node, ast.AsyncFunctionDef),
        decorators=tuple(_expression_name(decorator) for decorator in node.decorator_list),
    )


def _expression_name(node: ast.expr) -> str:
    """Render common declaration expressions without evaluating source code."""
    rendered = ast.unparse(node)
    return rendered
