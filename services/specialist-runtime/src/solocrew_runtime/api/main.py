from fastapi import Depends, FastAPI, HTTPException, status

from solocrew_runtime import __version__
from solocrew_runtime.api.security import (
    internal_token_is_configured,
    require_internal_worker_access,
)
from solocrew_runtime.contracts.research import (
    ResearchExecutionRequest,
    ResearchExecutionResponse,
)
from solocrew_runtime.research.graph import execute_research_request

app = FastAPI(
    title="SoloCrew Specialist Runtime",
    version=__version__,
    description="Private execution API for SoloCrew digital specialists.",
    docs_url=None,
    redoc_url=None,
    openapi_url=None,
)


@app.get("/health/liveness")
def liveness() -> dict[str, str]:
    """Report whether the runtime process is alive."""

    return {
        "status": "UP",
        "service": "solocrew-specialist-runtime",
        "version": __version__,
    }


@app.get("/health/readiness")
def readiness() -> dict[str, str]:
    """Report whether the runtime is configured to accept work."""

    if not internal_token_is_configured():
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Internal execution authentication is not configured.",
        )

    return {
        "status": "READY",
        "service": "solocrew-specialist-runtime",
    }


@app.post(
    "/internal/v1/research/execute",
    response_model=ResearchExecutionResponse,
    dependencies=[Depends(require_internal_worker_access)],
)
def execute_research(
    request: ResearchExecutionRequest,
) -> ResearchExecutionResponse:
    """Run one deterministic Personal Research Specialist execution."""

    return execute_research_request(request)
