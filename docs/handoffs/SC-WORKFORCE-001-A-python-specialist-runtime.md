# SC-WORKFORCE-001-A — Python Specialist Runtime

- Status: Complete and merged
- Date: 2026-11-26
- Branch: feature/SC-WORKFORCE-001-personal-research-specialist

## Objective

Establish the Python execution foundation for SoloCrew's first
digital employee, the Personal Research Specialist.

The runtime must execute a deterministic research graph without
requiring a language model, web-search provider or database.

## Delivered

- Python 3.14 local development support.
- Python project and dependency management through uv.
- FastAPI internal HTTP application.
- Pydantic request and response contracts.
- Internal bearer-token protection.
- Liveness and readiness endpoints.
- LangGraph deterministic research graph.
- Evidence extraction from supplied source excerpts.
- Evidence-to-source traceability.
- Unsupported-question and limitation reporting.
- Pytest tests.
- Ruff lint and formatting validation.
- Python CI validation foundation.

## Deliberately excluded

- Java-to-Python integration.
- Temporal implementation.
- PostgreSQL persistence.
- Owner Workspace research assignments.
- External web search.
- Web crawling or full-document retrieval.
- Ollama integration.
- Paid model providers.
- AI-generated research synthesis.

## Runtime contract

Endpoint:

    POST /internal/v1/research/execute

Authentication:

    Authorization: Bearer <SOLOCREW_INTERNAL_TOKEN>

The request includes:

- Assignment ID.
- Execution ID.
- Research topic.
- Research questions.
- Caller-supplied source excerpts.

The response includes:

- Assignment and execution IDs.
- Execution status.
- Brief title and summary.
- One finding per research question.
- Supporting evidence with source IDs.
- Limitations.
- Source count.
- Generation timestamp.

## Local validation

The owner validated the runtime using Python 3.14.7.

Recorded results:

- 11 Python tests passed.
- Ruff lint passed.
- Ruff formatting passed.
- Liveness returned HTTP 200.
- Readiness returned HTTP 200.

LangSmith emitted deprecation warnings from dependency code.
They did not cause test failures.

## Next milestone

SC-WORKFORCE-001-B1 — Research Evaluation Foundation.

The deterministic specialist must be benchmarked before adding
source discovery, advanced analysis or AI capabilities.
