from fastapi import FastAPI
from app.api.v1.rules import router as rules_router
from app.core.config import settings

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="Inference engine and vector search microservice powered by FastAPI and Qdrant.",
    version=settings.PROJECT_VERSION,
)

# Register API routers
app.include_router(rules_router)


@app.get("/")
def read_root():
    """
    Root health check endpoint to verify microservice availability.
    """
    return {"service": "as-sentinel-ai", "status": "running"}