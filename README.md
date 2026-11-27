# SoloCrew

**Your business. Your digital workforce.**

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

The product is designed around business outcomes and accountable
digital employees, not around reproducing a conventional ERP experience.

The Owner Command Center is the primary experience through which
business owners will delegate work, follow progress and review outcomes.

SoloCrew operators will use a separate Control Center.

## Repository layout

    apps/
      owner-web/
      control-center/

    backend/
      Java / Spring Boot modular monolith

    services/
      specialist-runtime/
        Python / LangGraph specialist execution

    docs/
      decisions/
      development/
      handoffs/

    scripts/

## Engineering baseline

| Tool | Version |
|---|---:|
| Java | 21 LTS |
| Gradle | 9.9.2 |
| Spring Boot | 4.2.0 |
| Spring Modulith | 2.2.0 |
| Node.js | 24 LTS |
| pnpm | 11.29.0 |
| Next.js | 16.4.6 |
| React | 19.3.1 |
| TypeScript | 5.9.3 |
| PostgreSQL | 18.x |
| Python | 3.13 and 3.14 |
| Python dependency manager | uv |
| Specialist graph runtime | LangGraph 1.x |

Spring AI 2.1.0 is recorded in ADR-0001 as a potential Java-side
model integration. It is not currently used by the Personal
Research Specialist.

See `docs/decisions/` for architecture decisions.

See `docs/development/local-setup.md` for workstation setup.

## Architecture principles

- Java owns authoritative business data and business workflows.
- Python executes specialist-specific responsibilities.
- Temporal Java SDK is selected for durable assignment lifecycle.
- LangGraph executes bounded specialist graphs.
- Deterministic capabilities are implemented before AI enhancements.
- AI is introduced only where evaluation demonstrates value.
- Specialist execution must not depend on paid model availability.
- The owner remains in control of consequential decisions.
- Business data remains independent of AI-provider state.
- No local Docker Compose dependency.

## Local development

Docker Compose is not required.

### Validate the toolchain

    ./scripts/doctor.sh

### Install frontend workspace dependencies

    pnpm install --frozen-lockfile

### Configure PostgreSQL

Install and start PostgreSQL 18 locally.

Create these databases:

- solocrew_dev
- solocrew_test

Create the application login:

- solocrew_app

See `docs/development/local-setup.md` for complete commands and
DBeaver connection details.

### Configure local secrets

Create a local copy:

    cp backend/local-secrets.properties.example \
       backend/local-secrets.properties

Edit the copied file and set your local PostgreSQL password.

Do not commit the local secrets file.

### Start the backend

    cd backend
    ./gradlew bootRun

The development profile connects to solocrew_dev.

Backend URL:

    http://localhost:8080

Health endpoints:

    http://localhost:8080/actuator/health
    http://localhost:8080/actuator/health/liveness
    http://localhost:8080/actuator/health/readiness

### Start the Owner Web application

From the repository root:

    pnpm run dev:owner-web

Owner Workspace:

    http://localhost:3000

### Start the Python specialist runtime

Install uv with Homebrew:

    brew install uv

Python 3.13 and 3.14 are supported.

Install Python 3.14 through uv if required:

    uv python install 3.14

From the repository root:

    cd services/specialist-runtime
    uv sync --locked --group dev

Run Python tests:

    uv run --locked pytest

Run lint and formatting checks:

    uv run --locked ruff check .
    uv run --locked ruff format --check .

### Evaluate the deterministic research specialist

From `services/specialist-runtime`:

    uv run --locked python \
      -m solocrew_runtime.evaluation.run_benchmark

The initial benchmark is:

    evaluation/benchmarks/research-v1.json

It measures evidence precision and recall, answerability,
citation traceability and execution latency.

Evaluation results must be reviewed before changing the
deterministic graph or introducing AI enhancements.

### Run the Python runtime

Generate a local internal token:

    export SOLOCREW_INTERNAL_TOKEN="$(openssl rand -hex 32)"

Start the service:

    uv run uvicorn solocrew_runtime.api.main:app \
      --host 127.0.0.1 \
      --port 8091

The runtime is private and is not intended to be exposed directly
to a browser or the public internet.

### Validate the frontend

    pnpm --filter @solocrew/owner-web typecheck
    pnpm --filter @solocrew/owner-web build

### Validate the backend

From the backend directory:

    ./gradlew clean check

Backend tests use solocrew_test.

### Validate the repository

From the repository root:

    pnpm run check

Python validation is performed by the dedicated CI job across
Python 3.13 and 3.14.

## Product principles

- One initial business type.
- Product-led discovery.
- Small, complete vertical slices.
- Validate workflows with the actual business.
- Modular monolith before microservices.
- The Owner Command Center is the primary owner experience.
- Business records remain authoritative.
- Digital employees have explicit responsibilities.
- Deterministic capability first.
- Measure performance before adding AI.
- Human accountability is preserved.
- No infrastructure dependency without a demonstrated need.

## Project documentation

- `SOLOCREW_HANDOFF.md`: Current project state and next action.
- `docs/handoffs/`: Completed slice handoff records.
- `docs/decisions/`: Long-lived architecture decisions.
- `docs/development/`: Workstation and development guides.
