from fastapi import FastAPI

app = FastAPI(
    title="AS-Sentinel AI Service",
    description="Microservei d'IA i RAG per a l'auditoria d'arquitectura i seguretat",
    version="0.1.0"
)

@app.get("/")
def read_root():
    return {"service": "as-sentinel-ai", "status": "online"}

@app.get("/health")
def health_check():
    return {"status": "healthy", "vector_db": "ready"}