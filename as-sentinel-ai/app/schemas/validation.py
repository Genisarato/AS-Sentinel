from typing import List, Optional
from pydantic import BaseModel, Field


class RuleViolation(BaseModel):
    rule_id: str = Field(..., description="ID de la regla o ADR infringida (ex: ADR-001)")
    severity: str = Field(..., description="Nivell de gravetat: error, warning, info")
    reason: str = Field(..., description="Explicació tècnica clara de per què el fitxer infringeix la regla")
    suggestion: str = Field(..., description="Com refactoritzar o corregir el codi per complir la regla")

class FileValidationResult(BaseModel):
    file_path: str
    is_compliant: bool = Field(..., description="True si no hi ha cap violació d'arquitectura, False en cas contrari")
    violations: List[RuleViolation] = Field(default_factory=list)

class ProjectAuditRequest(BaseModel):
    directory_path: str = Field(..., description="Ruta del projecte a auditar")
    top_k_rules: int = Field(default=3, ge=1, le=10)
    score_threshold: float = Field(default=0.35, ge=0.0, le=1.0)

class ProjectAuditResponse(BaseModel):
    directory_path: str
    total_files_scanned: int
    total_violations: int
    is_project_compliant: bool
    results: List[FileValidationResult]