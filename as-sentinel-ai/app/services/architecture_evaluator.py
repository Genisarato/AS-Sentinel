import json
import httpx
from typing import List, Dict, Any
from app.core.config import settings
from app.schemas.validation import FileValidationResult, RuleViolation


class ArchitectureEvaluator:
    """
    Uses a local SLM/LLM via Ollama to evaluate parsed AST file context
    against retrieved architectural rules from Qdrant.
    """

    def __init__(
        self,
        ollama_base_url: str | None = None,
        model_name: str | None = None,
    ):
        self.base_url = (ollama_base_url or settings.OLLAMA_BASE_URL).rstrip("/")
        self.model_name = model_name or settings.OLLAMA_MODEL

    def evaluate_file(
        self,
        file_path: str,
        file_context: str,
        retrieved_rules: List[Dict[str, Any]]
    ) -> FileValidationResult:
        # Short-circuit: if no architectural rules match this file's context, it's compliant
        if not retrieved_rules:
            return FileValidationResult(file_path=file_path, is_compliant=True, violations=[])

        rules_block = "\n\n".join(
            f"- [{r['rule_id']}] {r['title']} (Severity: {r['severity']}): {r['content']}"
            for r in retrieved_rules
        )

        system_prompt = (
            "You are AS-Sentinel, a strict automated Software Architecture Auditor. "
            "Your task is to analyze a Python file's structural AST summary against a set of architectural rules (ADRs). "
            "Only flag clear, undeniable architectural violations based on the file path, imports, and class structure. "
            "Respond strictly in JSON matching the requested schema."
        )

        user_prompt = (
            f"### TARGET FILE STRUCTURAL SUMMARY:\n{file_context}\n\n"
            f"### APPLICABLE ARCHITECTURAL RULES:\n{rules_block}\n\n"
            "Evaluate whether the target file violates any of the applicable rules above."
        )

        payload = {
            "model": self.model_name,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "stream": False,
            "format": FileValidationResult.model_json_schema(),
            "options": {
                "temperature": 0.0  # Deterministic output
            }
        }

        try:
            with httpx.Client(timeout=60.0) as client:
                response = client.post(f"{self.base_url}/api/chat", json=payload)
                response.raise_for_status()
        except httpx.RequestError as error:
            raise RuntimeError(
                f"Unable to connect to Ollama at {self.base_url}: {error}"
            ) from error
        except httpx.HTTPStatusError as error:
            raise RuntimeError(
                f"Ollama returned HTTP {error.response.status_code}: "
                f"{error.response.text}"
            ) from error

        try:
            response_data = response.json()
            raw_content = response_data["message"]["content"]
        except (json.JSONDecodeError, KeyError, TypeError) as error:
            raise RuntimeError(
                "Ollama returned an incomplete or invalid response: "
                "expected message.content"
            ) from error

        try:
            parsed_data = json.loads(raw_content)
        except (json.JSONDecodeError, TypeError) as error:
            raise RuntimeError(
                "Ollama returned invalid JSON in message.content"
            ) from error

        if not isinstance(parsed_data, dict):
            raise RuntimeError("Ollama response JSON must be an object")

        violations = [RuleViolation(**v) for v in parsed_data.get("violations", [])]
        is_compliant = len(violations) == 0

        return FileValidationResult(
            file_path=file_path,
            is_compliant=is_compliant,
            violations=violations
        )