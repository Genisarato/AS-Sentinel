"""Compatibility exports for the consolidated Qdrant vector store."""

from app.vector_store.qdrant import QdrantVectorStore, vector_store

__all__ = ["QdrantVectorStore", "vector_store"]
