import os
from fastapi import APIRouter, HTTPException, status
from app.schemas.ast import ASTScanRequest, ASTScanResponse
from app.services.project_scanner import ProjectScanner

router = APIRouter()


@router.post("/structure", response_model=ASTScanResponse, status_code=status.HTTP_200_OK)
def scan_project_structure(payload: ASTScanRequest):
    """
    Scans a project directory and extracts AST architectural structures from Python files.
    """
    if not os.path.exists(payload.directory_path):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Directory '{payload.directory_path}' not found."
        )

    scanner = ProjectScanner(payload.directory_path)
    results = scanner.scan()

    return ASTScanResponse(
        total_files=len(results),
        results=results
    )