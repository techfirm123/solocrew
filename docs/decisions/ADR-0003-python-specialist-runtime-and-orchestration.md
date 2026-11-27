# ADR-0003: Python Specialist Runtime and Orchestration Boundaries

- Status: Accepted
- Date: 2026-11-26
- Scope: Personal Research Specialist and future Python-based specialists

## Context

SoloCrew's authoritative application platform is a Java 21 and
Spring Boot modular monolith.

The product also needs access to the Python ecosystem for specialist
execution, research processing and future AI capabilities.

The architecture must avoid:

- Giving two runtimes ownership of the same business workflow.
- Duplicating business data and authorization.
- Running a generic agent framework for every business operation.
- Introducing unnecessary services and infrastructure.
- Making AI availability a prerequisite for specialist execution.

ADR-0001 selects the Temporal Java SDK for durable workflow
orchestration. This decision retains that direction.

## Decisions

### Java remains the authoritative platform

The Java/Spring Boot backend owns:

- Owner authentication and authorization.
- Business workspace and membership context.
- Assignment creation and lifecycle.
- Business records and PostgreSQL persistence.
- Audit events.
- Approval boundaries.
- Durable workflow orchestration through Temporal.

Python must not connect directly to the SoloCrew business database.

Python must not make independent authorization decisions about
business users or workspaces.

### Python is a specialist execution runtime

The separate runtime is located at:

    services/specialist-runtime/

The runtime uses:

- Python 3.13 and 3.14.
- uv for dependency and environment management.
- FastAPI for the private service boundary.
- Pydantic for typed and validated contracts.
- LangGraph for specialist execution graphs.
- Pytest and Ruff for validation.

The runtime is a separately executable process, not a second
business application or a replacement for the Java backend.

### Temporal and LangGraph have separate responsibilities

Temporal owns the lifecycle of an assignment:

- Durable execution.
- Retries and timeouts.
- Scheduling.
- Cancellation.
- Recovery after worker interruption.
- Waiting for external or human decisions.
- Assignment-level execution history.

LangGraph owns the execution graph inside a specialist task:

- Deterministic processing steps.
- Conditional routing.
- Optional model-driven steps.
- Evidence collection and transformation.
- Structured specialist results.

The intended integration boundary is:

    Owner Web
       |
       v
    Java / Spring Boot
       |
       v
    Temporal Java Workflow
       |
       v
    Java Activity
       |
       | Private authenticated HTTP contract
       v
    Python / FastAPI
       |
       v
    LangGraph Specialist Graph
       |
       v
    Structured Result
       |
       v
    Java validates and persists the result

Temporal is selected but is not yet implemented in the backend.

The first Python runtime slice does not add Temporal dependencies.

The Temporal Python SDK is not part of the initial specialist
execution architecture.

The same assignment lifecycle must not be implemented independently
in Java and Python.

### Deterministic-first specialist execution

Specialist execution must continue to provide useful behaviour
without an LLM provider.

Use conventional code for:

- Input validation.
- Text normalization.
- Evidence extraction.
- Ranking and deterministic calculations.
- Source-to-finding traceability.
- Report structure.
- Status and limitation reporting.

AI is introduced only where language understanding, reasoning
or synthesis provides measurable additional value.

Every optional AI step must have:

- An explicit timeout.
- Bounded retries.
- Output validation.
- A deterministic fallback or an honest partial result.
- Usage and cost visibility when applicable.

There must be no automatic switch to a paid model provider.

### AI provider direction

Ollama is the preferred initial local model provider.

Ollama integration is not part of the first Python runtime slice.

Spring AI is not used to implement the Personal Research Specialist.
Any future Java-side AI use case must justify a separate integration
rather than duplicating the specialist runtime.

### Data and execution boundaries

- Java creates and authorizes assignments.
- Java supplies the trusted execution request.
- Python validates the request contract.
- Python returns a structured result.
- Java validates the returned contract again.
- Java persists assignment results and audit events.
- Python does not write business records.
- Python does not receive database credentials.

The initial internal HTTP contract uses a shared bearer token.
Local development binds to loopback.

Production must keep the runtime on a private network and protect
its credentials through the deployment's secret-management mechanism.

### Research source discovery

The first research graph processes excerpts supplied by its caller.

External search and document retrieval will be introduced through
explicit source-provider adapters.

Search providers and LLM providers are separate integrations.
Neither is required for deterministic graph execution.

### Graph persistence

The initial graph is a bounded execution and does not require a
separate checkpoint database.

Temporal will own durable assignment-level lifecycle when it is
implemented.

A LangGraph checkpointer may be introduced if specialist requirements
demonstrate a need to resume at individual graph-node boundaries.

## Consequences

- The monorepo contains a Python runtime alongside the Java backend.
- The runtime has its own dependency lockfile and test suite.
- Java/Python communication uses an explicit versioned internal API.
- The first integration is a private HTTP call from a Java Activity.
- Specialist execution can evolve without transferring ownership
  of business data or permissions to Python.
- Deterministic specialist capability is developed and evaluated
  before AI enhancements.

## Validation required

- Python contract validation.
- Deterministic graph tests.
- Versioned research evaluation benchmarks.
- Internal token enforcement.
- Loopback binding for local development.
- Java validation of every specialist response.
- Idempotent assignment execution.
- Execution and correlation ID traceability.
- Honest reporting of runtime failures.

## Revisit this decision when

- A specialist requires durable node-level graph resumption.
- A specialist needs long-running external tool operations.
- The internal HTTP boundary becomes an operational bottleneck.
- A concrete Java-side AI use case justifies Spring AI.
- Security requirements require mTLS or workload identity.
