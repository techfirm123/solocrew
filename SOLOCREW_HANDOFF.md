# SoloCrew — Current Project Handoff

Last updated: 2026-11-25

## Current status

- Completed: SC-FOUNDATION-001 — Engineering Foundation
- Completed: SC-FOUNDATION-002 — Backend Application Foundation
- In progress: SC-FOUNDATION-003 — Persistence Foundation

Current feature branch:

    feature/SC-FOUNDATION-003-persistence-foundation

## Product context

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

It is not intended to become another conventional ERP.

The owner interacts with the digital workforce through the
Owner Command Center.

SoloCrew platform operators use a separate SoloCrew Control Center.

## Architecture direction

- Modular monolith initially.
- Java and Spring Boot backend.
- Next.js and TypeScript web applications.
- PostgreSQL as the authoritative relational data store.
- Flyway for versioned database changes.
- Temporal for durable workflow execution.
- Spring AI as an AI integration layer, not the application
  orchestrator.
- Local-first development without a Docker Compose dependency.
- Business operations and platform administration have separate
  security boundaries.

See docs/decisions/ADR-0001-engineering-baseline.md for the
engineering baseline.

## Toolchain

| Component | Version |
|---|---:|
| Java | 21 LTS |
| Gradle | 9.9.2 |
| Node.js | 24 LTS |
| pnpm | 11.29.0 |
| Spring Boot | 4.2.0 |
| Spring Modulith | 2.2.0 |
| Spring AI | 2.1.0 |
| Next.js | 16.4.6 |
| PostgreSQL | 18.x |

## Current local environment

- macOS with Homebrew.
- PostgreSQL 18.6 installed.
- PostgreSQL service started.
- pg_isready reports that port 5432 is accepting connections.
- DBeaver is available for database inspection.
- Local application role and databases are being configured.

## Completed slices

Detailed implementation and validation records:

- docs/handoffs/SC-FOUNDATION-001-engineering-foundation.md
- docs/handoffs/SC-FOUNDATION-002-backend-foundation.md

## Active slice — SC-FOUNDATION-003

Persistence Foundation:

- PostgreSQL JDBC connectivity.
- Hikari connection pooling.
- Flyway migration management.
- Initial platform schema.
- JDBC and transaction integration tests.
- Separate development and test databases.
- PostgreSQL-backed GitHub Actions validation.
- Local setup and DBeaver documentation.

No business tables are introduced in this slice.

## Development workflow

- Keep main stable.
- Use one feature branch per slice.
- Deliver small, complete vertical slices.
- Provide complete file contents for every created or changed file.
- Validate locally before opening a pull request.
- Validate CI before merging.
- Record completed work in docs/handoffs/.
- Keep this root document focused on current state and next actions.
- Keep architecture decisions in docs/decisions/.

## Immediate next actions

1. Complete local PostgreSQL role and database setup.
2. Apply the SC-FOUNDATION-003 implementation.
3. Run backend integration tests against solocrew_test.
4. Start the application against solocrew_dev.
5. Inspect the platform schema and Flyway history in DBeaver.
6. Validate GitHub Actions and merge the slice.

## Not yet implemented

- Authentication and authorization.
- Organization management.
- Workforce Manager.
- Digital employee execution.
- Personal Research Specialist.
- Temporal workflows.
- AI Model Gateway.
- Owner Web application screens.
- SoloCrew Control Center screens.
