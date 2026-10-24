from typing import List, Optional
from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field
from app.vector_store.qdrant import (
    get_qdrant_status,
    init_rules_collection_store,
)

router = APIRouter(prefix="/api/v1/rules", tags=["Architecture Rules"])


class RuleIngestionRequest(BaseModel):
    rule_id: str = Field(
        ..., description="Unique identifier for the rule (e.g., RULE-001)"
    )
    title: str = Field(..., description="Short title of the architectural rule")
    description: str = Field(
        ..., description="Detailed explanation of the rule or constraint"
    )
    severity: str = Field(
        default="MEDIUM",
        description="Severity level: LOW, MEDIUM, HIGH, CRITICAL",
    )
    tags: List[str] = Field(
        default=[], description="Categories like ['java', 'ddd', 'security']"
    )
    vector: Optional[List[float]] = Field(
        default=None, description="Pre-computed embedding vector"
    )


@router.get("/health/qdrant", status_code=status.HTTP_200_OK)
def check_qdrant_health():
    """
    Health check endpoint to verify connection with Qdrant Vector Store.
    """
    try:
        return get_qdrant_status()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to connect to Qdrant vector store: {str(e)}",
        )


@router.post("/init-collection", status_code=status.HTTP_201_CREATED)
def init_rules_collection():
    """
    Initializes the architecture rules vector collection in Qdrant.
    """
    try:
        return init_rules_collection_store()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to initialize collection: {str(e)}",
        )