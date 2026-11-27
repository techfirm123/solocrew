# SoloCrew Specialist Runtime

Private Python execution runtime for SoloCrew digital specialists.

The first capability is the Personal Research Specialist.

## Technology

- Python 3.13 and 3.14
- uv for Python project and dependency management
- FastAPI for the internal HTTP boundary
- Pydantic for typed and validated contracts
- LangGraph for specialist execution graphs
- Pytest for tests
- Ruff for linting and formatting

## Architecture boundary

The Java/Spring Boot backend remains authoritative for:

- Owner authentication and authorization.
- Business workspaces and memberships.
- Work assignments and assignment lifecycle.
- Business data and PostgreSQL persistence.
- Audit events.
- Durable assignment workflow orchestration through Temporal.

This Python runtime:

- Executes specialist-specific responsibilities.
- Accepts validated execution requests from trusted backend callers.
- Returns structured, validated results.
- Does not connect directly to the SoloCrew PostgreSQL database.
- Does not own owner authentication or business permissions.

The runtime is intended to be reachable only through a private network.
For local development, bind it to `127.0.0.1`.

## Personal Research Specialist

The current deterministic graph:

1. Receives a research topic and questions.
2. Analyses supplied source excerpts.
3. Finds candidate evidence using deterministic term matching.
4. Associates evidence with each research question.
5. Reports unsupported questions as evidence gaps.
6. Returns a structured research brief.

It does not yet:

- Search the internet.
- Download or crawl external pages.
- Call Ollama or another language model.
- Generate unsupported answers.
- Persist assignments or reports.

## Evaluation

The evaluation framework measures the deterministic graph against
versioned benchmark datasets.

Initial benchmark:

    evaluation/benchmarks/research-v1.json

Run it from this directory:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
