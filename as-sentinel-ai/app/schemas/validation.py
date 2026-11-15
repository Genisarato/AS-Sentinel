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