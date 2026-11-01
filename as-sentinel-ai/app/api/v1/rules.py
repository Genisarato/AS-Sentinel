import logging
from typing import List, Optional

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

from app.vector_store.qdrant import vector_store

router = APIRouter(prefix="/api/v1/rules", tags=["Architecture Rules"])
logger = logging.getLogger(__name__)


class RuleIngestionRequest(BaseModel):
    rule_id: str = Field(
        ...,
        min_length=1,
        max_length=200,
        description="Unique identifier for the rule (e.g., ADR-0001 or RULE-001)",
    )
    title: str = Field(
        ..., min_length=1, max_length=500, description="Short title of the architectural rule"
    )
    description: str = Field(
        ...,
        min_length=1,
        max_length=100_000,
        description="Detailed explanation of the rule, context, or constraint",
    )
    severity: str = Field(
        default="MEDIUM",
        description="Severity level: LOW, MEDIUM, HIGH, CRITICAL",
    )
    tags: List[str] = Field(
        default_factory=list, description="Categories like ['java', 'ddd', 'security']"
    )


class RuleIngestionResponse(BaseModel):
    status: str = Field(..., description="Status of the ingestion process")
    point_id: str = Field(..., description="Generated UUID point identifier in Qdrant")
    rule_id: str = Field(..., description="Associated rule identifier")


class RuleSearchQuery(BaseModel):
    query: str = Field(..., min_length=1, max_length=2_000)
    limit: int = Field(default=3, ge=1, le=50)


class RuleSearchResult(BaseModel):
    rule_id: Optional[str]
    title: Optional[str]
    content: Optional[str]
    category: Optional[str]
    tags: List[str]
    score: float


class RuleSearchResponse(BaseModel):
    query: str
    results: List[RuleSearchResult]


@router.get("/health/qdrant", status_code=status.HTTP_200_OK)
def check_qdrant_health():
    """
    Health check endpoint to verify connection with Qdrant Vector Store.
    """
    try:
        return vector_store.get_status()
    except Exception:
        logger.exception("Qdrant health check failed")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to connect to Qdrant vector store.",
        )


@router.post("/init-collection", status_code=status.HTTP_201_CREATED)
def init_rules_collection():
    """
    Initializes the architecture rules vector collection in Qdrant.
    """
    try:
        result = vector_store.init_collection()
        return {"status": result, "collection": vector_store.collection_name}
    except Exception:
        logger.exception("Qdrant collection initialization failed")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to initialize Qdrant collection.",
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
            category=request.severity,
            tags=request.tags,
        )
        return RuleIngestionResponse(
            status="indexed",
            point_id=point_id,
            rule_id=request.rule_id
        )
    except Exception:
        logger.exception("Rule ingestion failed for %s", request.rule_id)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to ingest rule '{request.rule_id}' into Qdrant.",
        )


@router.post("/search", response_model=RuleSearchResponse)
def search_rules(payload: RuleSearchQuery):
    """Semantic search endpoint to retrieve architecture rules from Qdrant."""
    results = vector_store.search_similar_rules(
        query=payload.query, limit=payload.limit
    )
    return {"query": payload.query, "results": results}