
# SoloCrew — Current Project Handoff

Last updated: 2026-11-27

## Current status

| Slice | Status |
|---|---|
| SC-FOUNDATION-001 — Engineering Foundation | Complete |
| SC-FOUNDATION-002 — Backend Application Foundation | Complete |
| SC-FOUNDATION-003 — Persistence Foundation | Complete |
| SC-FOUNDATION-004 — Owner Workspace & Command Center | Complete |
| SC-FOUNDATION-005-A — Identity & Security Foundation | Complete |
| SC-FOUNDATION-005-B — Registration & Email Verification | Complete |
| SC-FOUNDATION-005-C1 — Backend Owner Login & Protected Workspace | Complete |
| SC-FOUNDATION-005-C2 — Owner Web Authentication & Access | Complete |
| SC-WORKFORCE-001-A — Python Specialist Runtime | Complete and merged |
| SC-WORKFORCE-001-B1 — Research Evaluation Foundation through B1.3 | Complete and merged |
| SC-WORKFORCE-001-B1.4 — Deterministic Matching Improvements | Implemented and locally validated; pending PR and merge |

Current branch:

    feature/SC-WORKFORCE-001-B1.4-deterministic-matching-improvements

The B1.4 branch includes both:

- B1.4.1 — Controlled Vocabulary Normalization
- B1.4.2 — Deterministic Question Intent and Relationship Matching

These changes have not yet been committed or merged.

## Product context

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

The product introduces specialized digital employees that take
responsibility for appropriate business activities.

The Owner Command Center is the primary experience for delegating
work, monitoring progress and reviewing deliverables.

The owner remains accountable for important business decisions.

SoloCrew is not intended to become a generic ERP, CRM or agent
marketplace.

## Product and engineering principles

- One business, one product and one clear purpose.
- Business-first development.
- Small, complete vertical slices.
- Build and validate deterministic capability first.
- Measure specialist performance.
- Introduce AI only where evaluation demonstrates value.
- Business records remain authoritative.
- Specialists operate through controlled capabilities.
- Important actions are auditable.
- Failures and uncertainty are reported honestly.
- Owner approval boundaries are preserved.
- No unnecessary services or infrastructure.
- Docker Compose is not a local development requirement.

## Architecture direction

### Core business platform

- Java 21.
- Spring Boot 4.2.0.
- Spring Modulith 2.2.0.
- Gradle 9.9.2.
- PostgreSQL 18.x.
- Flyway.
- Spring Security.
- Spring Session JDBC.
- Next.js, React and TypeScript.

The Java backend owns:

- Identity and authentication.
- Business workspace and membership.
- Business capabilities and records.
- Assignment lifecycle.
- Authorization.
- Audit events.
- Durable assignment workflow orchestration.

### Specialist execution

- Python 3.13 and 3.14.
- uv.
- FastAPI.
- Pydantic.
- LangGraph 1.x.
- Pytest.
- Ruff.

The Python runtime executes specialist-specific graphs and returns
structured results to trusted callers.

It does not own business data, owner authorization or assignment
persistence.

### Workflow and AI direction

- Temporal Java SDK is selected for durable assignment lifecycle.
- Temporal is not yet implemented in the backend.
- LangGraph executes bounded specialist graphs.
- The Temporal Python SDK is not part of the initial design.
- Ollama is the preferred local model provider for future AI work.
- AI is optional and must not be a prerequisite for core specialist
  responsibilities.
- There is no automatic switch to paid model providers.
- Spring AI is not currently used by the Personal Research Specialist.

Architecture decisions:

- `docs/decisions/ADR-0001-engineering-baseline.md`
- `docs/decisions/ADR-0002-identity-access-and-auditability.md`
- `docs/decisions/ADR-0003-python-specialist-runtime-and-orchestration.md`
- `docs/decisions/ADR-0004-deterministic-specialist-evaluation.md`

## Toolchain

| Component | Version |
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
| uv | Python dependency manager |
| LangGraph | 1.x |

## Local development environment

- macOS with Homebrew.
- Java 21.
- Node.js 24.
- pnpm 11.29.0.
- Gradle 9.9.2.
- PostgreSQL 18.6 installed and running natively.
- `solocrew_dev` and `solocrew_test` databases.
- `solocrew_app` application database role.
- DBeaver connections verified.
- Mailpit installed and running natively.
- Mailpit SMTP: `localhost:1025`.
- Mailpit web UI: `http://localhost:8025`.
- Python 3.14.7 available locally.
- uv manages the specialist runtime environment.

Docker Compose is not required for local development.

Local credentials and encryption keys must remain outside
tracked files.

## Completed foundation

### Backend and persistence

- Spring Boot application foundation.
- Spring Modulith module structure.
- PostgreSQL connection and Flyway.
- Platform schema.
- Identity and audit schema.
- Spring Session JDBC.
- Email verification and transactional email outbox.

Migrations V1–V4 are part of the existing foundation.

### Owner identity and access

- Owner registration.
- Email verification.
- Secure password hashing.
- Server-side owner sessions.
- Session ID rotation.
- CSRF protection.
- Login and logout APIs.
- Protected owner workspace access.
- Workspace membership checks.
- Authentication and audit events.

### Owner Web

Public routes:

- `/` — public SoloCrew landing page.
- `/login` — owner sign-in.
- `/register` — owner registration.
- `/verify-email` — email verification and recovery.

Protected route:

- `/workspace` — Owner Command Center.

SC-FOUNDATION-005-C2 was merged through PR #4 and visually
validated by the owner.

Theme foundation:

    midnight-botanical

Semantic design tokens:

    apps/owner-web/src/app/design-tokens.css
    docs/development/design-system.md

## Digital workforce — Personal Research Specialist

### SC-WORKFORCE-001-A — Python Specialist Runtime

Status: Complete and merged.

Location:

    services/specialist-runtime/

Delivered:

- Python project and dependency management through uv.
- FastAPI private HTTP runtime.
- Pydantic request and response contracts.
- Internal bearer-token protection.
- Liveness and readiness endpoints.
- LangGraph deterministic research graph.
- Evidence extraction from supplied source excerpts.
- Evidence-to-source traceability.
- Unsupported-question handling.
- Structured research result.
- Pytest and Ruff validation.

Initial foundation validation:

- 11 Python tests passed.
- Ruff lint passed.
- Ruff formatting passed.
- Liveness returned HTTP 200.
- Readiness returned HTTP 200.

The original source-excerpt graph is a foundation, not a complete
research capability.

Canonical handoff:

    docs/handoffs/SC-WORKFORCE-001-A-python-specialist-runtime.md

### SC-WORKFORCE-001-B1 — Research Evaluation Foundation

Status: Core evaluation foundation through B1.3 complete and merged.

Objective:

Establish reproducible evaluation of deterministic research behaviour
before expanding the specialist.

Controlled benchmark:

    services/specialist-runtime/evaluation/benchmarks/research-v1.json

Challenge benchmark:

    services/specialist-runtime/evaluation/benchmarks/research-challenge-v1.json

The controlled benchmark covers:

1. Laptop battery health.
2. Laptop overheating.
3. SSD reliability.

It contains 13 questions, including deliberately unanswerable
questions.

The challenge benchmark contains five scenarios covering:

1. Paraphrased questions.
2. Short keyword overlap.
3. High-overlap distractors.
4. Multi-sentence evidence.
5. Unsupported-question rejection.

Evaluation capabilities:

- Evidence precision.
- Evidence recall.
- Evidence F1.
- Question answerability accuracy.
- Answerable question recall.
- Unsupported-question rejection.
- False support rate.
- Citation traceability.
- Execution latency.
- Question-level diagnostics.
- Candidate sentence selection diagnostics.
- Status consistency checks.
- Selection consistency checks.

### SC-WORKFORCE-001-B1.4 — Deterministic Matching Improvements

Status: Implemented and locally validated; pending PR and merge.

Active branch:

    feature/SC-WORKFORCE-001-B1.4-deterministic-matching-improvements

#### B1.4.1 — Controlled Vocabulary Normalization

Introduced shared normalization and controlled concept aliases.

Revision:

    controlled-concept-aliases-v1

The graph and evaluation diagnostics use shared normalization
behaviour. Original source sentences remain unchanged in returned
evidence.

This increment addressed a vocabulary mismatch in the paraphrased
thermal research question.

#### B1.4.2 — Deterministic Question Intent and Relationship Matching

Introduced bounded subject-predicate-outcome extraction for
recognized yes/no impact questions.

Revision:

    deterministic-outcome-intent-v1

For recognized patterns, candidate evidence must satisfy the
subject and requested outcome constraints in addition to the
existing lexical relevance thresholds.

This specifically addresses misleading keyword overlap where a
sentence discusses the subject but not the outcome being asked
about.

The implementation remains deterministic and does not use an LLM
or embeddings.

#### Current graph revision

    deterministic-question-focused-v4

#### Validation results

Local environment:

    Python 3.14.7

Validation:

- 44 Python tests passed.
- Ruff lint passed.
- Ruff formatting passed.
- Controlled benchmark passed.
- Challenge benchmark passed.

Controlled benchmark:

| Metric | Result |
|---|---:|
| Questions | 13 |
| Evidence true positives | 10 |
| Evidence false positives | 0 |
| Evidence false negatives | 0 |
| Evidence precision | 1.0000 |
| Evidence recall | 1.0000 |
| Evidence F1 | 1.0000 |
| Answerability accuracy | 1.0000 |
| Unsupported-question rejection | 1.0000 |
| False support rate | 0.0000 |
| Citation traceability | 10/10 |
| Status consistency errors | 0 |
| Average case execution latency | 1.01 ms |

Challenge benchmark:

| Metric | Result |
|---|---:|
| Questions | 5 |
| Evidence true positives | 3 |
| Evidence false positives | 0 |
| Evidence false negatives | 0 |
| Evidence precision | 1.0000 |
| Evidence recall | 1.0000 |
| Evidence F1 | 1.0000 |
| Answerability true positives | 2 |
| Answerability true negatives | 3 |
| Answerability false positives | 0 |
| Answerability false negatives | 0 |
| Answerability accuracy | 1.0000 |
| Unsupported-question rejection | 1.0000 |
| Status consistency errors | 0 |
| Selection consistency errors | 0 |

The five challenge classifications were:

- Paraphrased thermal question: TRUE_POSITIVE.
- Short keyword overlap: TRUE_NEGATIVE.
- High-overlap distractor: TRUE_NEGATIVE.
- Multi-sentence battery evidence: TRUE_POSITIVE.
- Unsupported screen/SSD question: TRUE_NEGATIVE.

The controlled and challenge datasets use synthetic source material.
These results validate deterministic behaviour against authored
fixtures; they do not establish factual correctness or general
semantic understanding.

The relationship matcher deliberately uses bounded patterns and
exact normalized outcome terms. Valid paraphrases using different
outcome vocabulary may still be rejected.

The latest test execution reports 17 LangSmith dependency
deprecation warnings. They are non-blocking and are not test failures.

Canonical increment handoffs:

    docs/handoffs/SC-WORKFORCE-001-B1.4.1-controlled-vocabulary-normalization.md

    docs/handoffs/SC-WORKFORCE-001-B1.4.2-deterministic-question-intent-and-relationship-matching.md

## Revised Personal Research Specialist roadmap

| Slice | Focus |
|---|---|
| 001-A | Python runtime foundation — complete |
| 001-B1 | Research evaluation foundation — merged |
| 001-B1.4 | Deterministic matching improvements — locally validated, pending merge |
| 001-B2 | Deterministic source discovery and retrieval |
| 001-B3 | Deterministic research analysis and evidence quality |
| 001-B4 | Research report generation |
| 001-C | Java assignment integration |
| 001-D | Optional AI evaluation and integration |

Java integration is deliberately deferred until the deterministic
specialist has a meaningful, evaluated capability.

## Not yet implemented

- Research source discovery.
- External search-provider adapters.
- Full-document retrieval and extraction.
- Advanced source-quality assessment.
- Comprehensive research synthesis.
- Java-to-Python specialist integration.
- Durable Temporal workflows.
- Research assignment persistence.
- Owner-facing delegation and result review.
- Ollama integration.
- AI Model Gateway.
- Other digital specialists.
- Business operational capabilities.
- SoloCrew Control Center.
- Mobile applications.

## Development conventions

- Keep `main` stable.
- Use one feature branch per slice.
- Build small, complete vertical slices.
- Validate each slice before proceeding.
- Provide complete contents for created or changed files.
- Do not use ZIP files as the code handoff.
- Do not assume Docker Compose is available.
- Do not claim tests passed until they have actually been run.
- Maintain versioned evaluation datasets.
- Do not change benchmark expectations simply to improve scores.
- Update root handoff and slice handoff documents.
- Keep architecture decisions in `docs/decisions/`.
- Keep local credentials out of tracked files.

## Immediate next action

Complete the B1.4 feature branch close-out:

1. Confirm the B1.4.1 and B1.4.2 handoff documents are present.
2. Update this root handoff.
3. Review the complete branch diff.
4. Run final validation and `git diff --check`.
5. Commit and push the feature branch.
6. Open a pull request targeting `main`.
7. Merge only after reviewing the PR.

After B1.4 is merged, begin SC-WORKFORCE-001-B2:
deterministic source discovery and retrieval.
