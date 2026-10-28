from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct
from sentence_transformers import SentenceTransformer
import uuid

class QdrantVectorStore:
    def __init__(self, host: str = "localhost", port: int = 6333):
        # Connectem amb el contenidor de Qdrant en Docker
        self.client = QdrantClient(host=host, port=port)
        # Carreguem un model lleuger i ràpid d'embeddings (384 dimensions)
        self.encoder = SentenceTransformer("all-MiniLM-L6-v2")
        self.collection_name = "architecture_rules"

    def init_collection(self) -> str:
        """Crea la col·lecció a Qdrant si no existeix directament."""
        collections = [c.name for c in self.client.get_collections().collections]
        if self.collection_name not in collections:
            self.client.create_collection(
                collection_name=self.collection_name,
                vectors_config=VectorParams(
                    size=384,  # La mida exacta dels vectors que genera all-MiniLM-L6-v2
                    distance=Distance.COSINE
                )
            )
            return "created"
        return "already_exists"

    def upsert_rule(self, rule_id: str, title: str, content: str, category: str = "ADR") -> str:
        """
        Genera l'embedding del contingut i desa el punt amb les seves metadades a Qdrant.
        """
        # 1. Generem el vector a partir del text del contingut
        vector = self.encoder.encode(content).tolist()

        # 2. Generem un UUID vàlid per a Qdrant a partir del rule_id (p.ex: "ADR-0001")
        point_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, rule_id))

        # 3. Creem el punt amb el seu payload (metadades del document)
        point = PointStruct(
            id=point_id,
            vector=vector,
            payload={
                "rule_id": rule_id,
                "title": title,
                "content": content,
                "category": category
            }
        )

        # 4. Guardem o actualitzem a Qdrant
        self.client.upsert(
            collection_name=self.collection_name,
            points=[point]
        )
        return point_id