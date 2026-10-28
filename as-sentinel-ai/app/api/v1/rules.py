from typing import List, Optional
from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field
from app.core.vector_store import QdrantVectorStore
from app.vector_store.qdrant import (
    get_qdrant_status,
    init_rules_collection_store,
)

router = APIRouter(prefix="/api/v1/rules", tags=["Architecture Rules"])
vector_store = QdrantVectorStore()


class RuleIngestionRequest(BaseModel):
    rule_id: str = Field(
        ..., description="Unique identifier for the rule (e.g., ADR-0001 or RULE-001)"
    )
    title: str = Field(..., description="Short title of the architectural rule")
    description: str = Field(
        ..., description="Detailed explanation of the rule, context, or constraint"
    )
    severity: str = Field(
        default="MEDIUM",
        description="Severity level: LOW, MEDIUM, HIGH, CRITICAL",
    )
    tags: List[str] = Field(
        default=[], description="Categories like ['java', 'ddd', 'security']"
    )
    vector: Optional[List[float]] = Field(
        default=None, description="Pre-computed embedding vector (optional)"
    )


class RuleIngestionResponse(BaseModel):
    status: str = Field(..., description="Status of the ingestion process")
    point_id: str = Field(..., description="Generated UUID point identifier in Qdrant")
    rule_id: str = Field(..., description="Associated rule identifier")


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


@router.post("/ingest", response_model=RuleIngestionResponse, status_code=status.HTTP_201_CREATED)
def ingest_rule(request: RuleIngestionRequest):
    """
    Ingest and index an architectural rule or ADR into the Qdrant vector store.
    Generates semantic embedding for the description if no pre-computed vector is provided.
    """
    try:
        point_id = vector_store.upsert_rule(
            rule_id=request.rule_id,
            title=request.title,
            content=request.description,
            category=request.severity
        )
        return RuleIngestionResponse(
            status="indexed",
            point_id=point_id,
            rule_id=request.rule_id
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to ingest rule '{request.rule_id}' into Qdrant: {str(e)}",
        )