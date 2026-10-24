import os


class Settings:
    PROJECT_NAME: str = "AS-Sentinel AI Service"
    PROJECT_VERSION: str = "0.1.0"

    # Qdrant configuration
    QDRANT_HOST: str = os.getenv("QDRANT_HOST", "localhost")
    QDRANT_PORT: int = int(os.getenv("QDRANT_PORT", 6333))
    DEFAULT_COLLECTION: str = "architecture_rules"
    VECTOR_SIZE: int = 384  # Embedding dimension (e.g., all-MiniLM-L6-v2)


settings = Settings()