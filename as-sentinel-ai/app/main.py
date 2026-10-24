from fastapi import FastAPI, HTTPException
from qdrant_client import QdrantClient
from qdrant_client.http.exceptions import UnexpectedResponse

app = FastAPI(
    title="AS-Sentinel AI Service",
    description="Microservei d'inferència i cerca vectorial amb Qdrant",
    version="0.1.0"
)

# Connectem amb el contenidor de Qdrant (localhost:6333)
qdrant_client = QdrantClient(host="localhost", port=6333)

COLLECTION_NAME = "architecture_rules"

@app.get("/")
def read_root():
    return {"service": "as-sentinel-ai", "status": "running"}

@app.get("/health/qdrant")
def check_qdrant():
    """Verifica la connexió amb Qdrant i crea la col·lecció de regles si no existeix."""
    try:
        collections = qdrant_client.get_collections()
        collection_names = [col.name for col in collections.collections]
        
        return {
            "qdrant_status": "connected",
            "existing_collections": collection_names
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error de connexió amb Qdrant: {str(e)}")