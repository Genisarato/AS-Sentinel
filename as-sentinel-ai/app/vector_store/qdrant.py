import uuid

import torch
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, PointStruct, VectorParams
from sentence_transformers import SentenceTransformer

from app.core.config import settings


class QdrantVectorStore:
    """Qdrant repository and embedding service for architecture rules."""

    def __init__(
        self,
        host: str = settings.QDRANT_HOST,
        port: int = settings.QDRANT_PORT,
        collection_name: str = settings.DEFAULT_COLLECTION,
        encoder: SentenceTransformer | None = None,
    ):
        self.client = QdrantClient(host=host, port=port)
        self.collection_name = collection_name
        self.encoder = encoder or SentenceTransformer(
            settings.EMBEDDING_MODEL,
            device="cuda" if torch.cuda.is_available() else "cpu",
        )

    def init_collection(self) -> str:
        collections = {
            collection.name for collection in self.client.get_collections().collections
        }
        if self.collection_name in collections:
            return "already_exists"

        self.client.create_collection(
            collection_name=self.collection_name,
            vectors_config=VectorParams(
                size=settings.VECTOR_SIZE,
                distance=Distance.COSINE,
            ),
        )
        return "created"

    def get_status(self) -> dict:
        collections = self.client.get_collections()
        return {
            "qdrant_status": "connected",
            "existing_collections": [
                collection.name for collection in collections.collections
            ],
        }

    def upsert_rule(
        self,
        rule_id: str,
        title: str,
        content: str,
        category: str = "ADR",
        tags: list[str] | None = None,
    ) -> str:
        vector = self.encoder.encode(content).tolist()
        point_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, rule_id))
        point = PointStruct(
            id=point_id,
            vector=vector,
            payload={
                "rule_id": rule_id,
                "title": title,
                "content": content,
                "category": category,
                "tags": tags or [],
            },
        )
        self.client.upsert(collection_name=self.collection_name, points=[point])
        return point_id

    def search_similar_rules(self, query: str, limit: int = 3) -> list[dict]:
        query_vector = self.encoder.encode(query).tolist()
        response = self.client.query_points(
            collection_name=self.collection_name,
            query=query_vector,
            limit=limit,
            with_payload=True,
        )
        return [
            {
                "rule_id": hit.payload.get("rule_id") if hit.payload else None,
                "title": hit.payload.get("title") if hit.payload else None,
                "content": hit.payload.get("content") if hit.payload else None,
                "category": hit.payload.get("category") if hit.payload else None,
                "tags": hit.payload.get("tags", []) if hit.payload else [],
                "score": hit.score,
            }
            for hit in response.points
        ]


vector_store = QdrantVectorStore()
