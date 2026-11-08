from pydantic import BaseModel, Field


class ImportItem(BaseModel):
    module: str
    names: list[str]
    lineno: int
    level: int
    is_relative: bool


class MethodItem(BaseModel):
    name: str
    lineno: int
    end_lineno: int | None
    is_async: bool
    decorators: list[str]


class ClassItem(BaseModel):
    name: str
    lineno: int
    end_lineno: int | None
    bases: list[str]
    decorators: list[str]
    docstring: str | None
    methods: list[MethodItem]


class FileASTSummary(BaseModel):
    file_path: str
    imports: list[ImportItem] = Field(default_factory=list)
    classes: list[ClassItem] = Field(default_factory=list)
    external_dependencies: list[str] = Field(default_factory=list)
    error: str | None = None


class ASTScanRequest(BaseModel):
    directory_path: str = Field(..., description="Ruta relativa o absoluta del directori a analitzar")


class ASTScanResponse(BaseModel):
    total_files: int
    results: list[FileASTSummary]