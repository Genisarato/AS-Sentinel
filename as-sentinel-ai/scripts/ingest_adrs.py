import torch
import uuid
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct
from sentence_transformers import SentenceTransformer


class QdrantVectorStore:
    def __init__(self, host: str = "localhost", port: int = 6333):
        self.client = QdrantClient(host=host, port=port)

        # Detect GPU acceleration
        device = "cuda" if torch.cuda.is_available() else "cpu"
        print(f"Loading SentenceTransformer model on device: {device}")

        self.encoder = SentenceTransformer("all-MiniLM-L6-v2", device=device)
        self.collection_name = "architecture_rules"

    def init_collection(self) -> str:
        collections = [c.name for c in self.client.get_collections().collections]
        if self.collection_name not in collections:
            self.client.create_collection(
                collection_name=self.collection_name,
                vectors_config=VectorParams(size=384, distance=Distance.COSINE),
            )
            return "created"
        return "already_exists"

    def upsert_rule(
        self, rule_id: str, title: str, content: str, category: str = "ADR"
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
            },
        )

        self.client.upsert(collection_name=self.collection_name, points=[point])
        return point_id