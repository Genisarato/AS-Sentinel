import os


class Settings:
    PROJECT_NAME: str = "AS-Sentinel AI Service"
    PROJECT_VERSION: str = "0.1.0"

    # Qdrant configuration
    QDRANT_HOST: str = os.getenv("QDRANT_HOST", "localhost")
    QDRANT_PORT: int = int(os.getenv("QDRANT_PORT", "6333"))
    DEFAULT_COLLECTION: str = "architecture_rules"
    EMBEDDING_MODEL: str = os.getenv(
        "EMBEDDING_MODEL", "sentence-transformers/all-MiniLM-L6-v2"
    )
    OLLAMA_BASE_URL: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
    OLLAMA_MODEL: str = os.getenv("OLLAMA_MODEL", "qwen2.5-coder:7b")
    VECTOR_SIZE: int = 384
    SEARCH_LIMIT_MIN: int = 1
    SEARCH_LIMIT_MAX: int = 50


settings = Settings()