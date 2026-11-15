from typing import List, Dict, Any
from qdrant_client import QdrantClient
from sentence_transformers import SentenceTransformer


class ArchitectureRuleRetriever:
    """
    Queries Qdrant vector store using semantic AST file context
    to retrieve relevant architectural rules and ADRs.
    """

    def __init__(
        self,
        qdrant_client: QdrantClient,
        embedding_model: SentenceTransformer,
        collection_name: str = "architecture_rules"
    ):
        self.client = qdrant_client
        self.model = embedding_model
        self.collection_name = collection_name

    def retrieve_relevant_rules(
        self,
        file_context: str,
        top_k: int = 3,
        score_threshold: float = 0.35
    ) -> List[Dict[str, Any]]:
        """
        Embeds the AST file context and retrieves matching rules from Qdrant.
        """
        query_vector = self.model.encode(file_context).tolist()

        search_results = self.client.query_points(
            collection_name=self.collection_name,
            query=query_vector,
            limit=top_k,
            score_threshold=score_threshold,
            with_payload=True
        ).points

        matched_rules = []
        for hit in search_results:
            payload = hit.payload or {}
            matched_rules.append({
                "rule_id": payload.get("rule_id", str(hit.id)),
                "title": payload.get("title", "Untitled Rule"),
                "content": payload.get("content", ""),
                "severity": payload.get(
                    "severity",
                    payload.get("category", "warning"),
                ),
                "score": round(hit.score, 4)
            })

        return matched_rules