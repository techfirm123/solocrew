# SoloCrew Specialist Runtime

Private Python execution runtime for SoloCrew digital specialists.

The first capability is the Personal Research Specialist's deterministic
research graph.

## Technology

- Python 3.13
- uv for Python project and dependency management
- FastAPI for the internal HTTP boundary
- Pydantic for typed request and response contracts
- LangGraph for specialist execution graphs
- Pytest for tests
- Ruff for linting and formatting

## Architecture boundary

The Java/Spring Boot backend remains authoritative for:

- Owner authentication and authorization
- Business workspaces and memberships
- Work assignments and assignment lifecycle
- Business data and PostgreSQL persistence
- Audit events
- Durable workflow orchestration through Temporal

This Python runtime:

- Executes specialist-specific work.
- Accepts validated execution requests from trusted backend callers.
- Returns structured, validated results.
- Does not connect directly to the SoloCrew PostgreSQL database.
- Does not own owner authentication or business permissions.

The runtime is intended to be reachable only through a private network.
For local development, bind it to `127.0.0.1`.

## Initial research behaviour

The first graph analyses source excerpts supplied in the execution request.

It:

1. Receives the research topic and questions.
2. Analyses the supplied excerpts.
3. Finds relevant sentences using deterministic term matching.
4. Associates evidence with each research question.
5. Reports unsupported questions as evidence gaps.
6. Returns a structured research brief.

It does not yet:

- Search the internet.
- Download or crawl external pages.
- Call Ollama or another language model.
- Generate unsupported answers.
- Persist assignments or reports.

## Prerequisites

Install uv with Homebrew:

```bash
brew install uv
