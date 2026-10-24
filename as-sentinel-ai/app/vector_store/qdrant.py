from qdrant_client import QdrantClient
from qdrant_client.http.models import Distance, VectorParams
from app.core.config import settings

# Global Qdrant client instance
qdrant_client = QdrantClient(host=settings.QDRANT_HOST, port=settings.QDRANT_PORT)


def get_qdrant_status() -> dict:
    """
    Retrieves Qdrant health status and lists existing collections.
    """
    collections = qdrant_client.get_collections()
    collection_names = [col.name for col in collections.collections]
    return {
        "qdrant_status": "connected",
        "existing_collections": collection_names,
    }


def init_rules_collection_store() -> dict:
    """
    Ensures that the default vector store collection exists with correct parameters.
    """
    collections = qdrant_client.get_collections()
    existing_names = [col.name for col in collections.collections]

    if settings.DEFAULT_COLLECTION not in existing_names:
        qdrant_client.create_collection(
            collection_name=settings.DEFAULT_COLLECTION,
            vectors_config=VectorParams(
                size=settings.VECTOR_SIZE, distance=Distance.COSINE
            ),
        )
        return {"status": "created", "collection": settings.DEFAULT_COLLECTION}

    return {
        "status": "already_exists",
        "collection": settings.DEFAULT_COLLECTION,
    }