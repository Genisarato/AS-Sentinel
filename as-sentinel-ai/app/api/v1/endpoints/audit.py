import os
from fastapi import APIRouter, HTTPException, status, Depends
from app.schemas.validation import ProjectAuditRequest, ProjectAuditResponse, FileValidationResult
from app.services.project_scanner import ProjectScanner
from app.services.context_builder import ASTContextBuilder
from app.services.rule_retriever import ArchitectureRuleRetriever
from app.services.architecture_evaluator import ArchitectureEvaluator

router = APIRouter()


# Dependències (adapta la inicialització de QdrantClient i SentenceTransformer al teu setup de Fase 1)
def get_rule_retriever() -> ArchitectureRuleRetriever:
    from qdrant_client import QdrantClient
    from sentence_transformers import SentenceTransformer
    client = QdrantClient(url=os.getenv("QDRANT_URL", "http://localhost:6333"))
    model = SentenceTransformer(os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2"))
    return ArchitectureRuleRetriever(qdrant_client=client, embedding_model=model)


def get_evaluator() -> ArchitectureEvaluator:
    return ArchitectureEvaluator(
        ollama_base_url=os.getenv("OLLAMA_BASE_URL", "http://localhost:11434"),
        model_name=os.getenv("OLLAMA_MODEL", "qwen2.5-coder:7b")
    )


@router.post("/", response_model=ProjectAuditResponse, status_code=status.HTTP_200_OK)
def audit_project(
    payload: ProjectAuditRequest,
    retriever: ArchitectureRuleRetriever = Depends(get_rule_retriever),
    evaluator: ArchitectureEvaluator = Depends(get_evaluator),
):
    if not os.path.exists(payload.directory_path):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Directory '{payload.directory_path}' not found."
        )

    scanner = ProjectScanner(payload.directory_path)
    scanned_files = scanner.scan()

    validation_results: list[FileValidationResult] = []
    total_violations = 0

    for file_summary in scanned_files:
        if "error" in file_summary:
            continue

        file_path = file_summary["file_path"]
        semantic_context = ASTContextBuilder.build_file_context(file_summary)
        applicable_rules = retriever.retrieve_relevant_rules(
            file_context=semantic_context,
            top_k=payload.top_k_rules,
            score_threshold=payload.score_threshold
        )

        file_result = evaluator.evaluate_file(
            file_path=file_path,
            file_context=semantic_context,
            retrieved_rules=applicable_rules
        )

        total_violations += len(file_result.violations)
        validation_results.append(file_result)

    return ProjectAuditResponse(
        directory_path=payload.directory_path,
        total_files_scanned=len(validation_results),
        total_violations=total_violations,
        is_project_compliant=(total_violations == 0),
        results=validation_results
    )