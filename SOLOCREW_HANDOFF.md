# SoloCrew — Current Project Handoff

Last updated: 2026-11-25

## Current status

- Complete: SC-FOUNDATION-001 — Engineering Foundation
- Complete: SC-FOUNDATION-002 — Backend Application Foundation
- Complete: SC-FOUNDATION-003 — Persistence Foundation
- In progress: SC-FOUNDATION-004 — Owner Workspace Foundation

Current feature branch:

    feature/SC-FOUNDATION-004-owner-command-center

## Product context

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

The product is not intended to become another conventional ERP.

The business owner interacts with SoloCrew through the Owner
Command Center.

SoloCrew platform operators use a separate SoloCrew Control Center.

The Owner Command Center is the primary product experience.
Digital employees and business capabilities should become available
through this experience as they are implemented.

## Architecture direction

- Modular monolith initially.
- Java 21 and Spring Boot backend.
- Next.js, React and TypeScript web applications.
- PostgreSQL as the authoritative relational data store.
- Flyway for versioned database changes.
- JDBC for direct persistence access until domain requirements
  justify additional persistence abstractions.
- Temporal for durable workflow execution.
- Spring AI as an AI integration layer, not the application
  orchestrator.
- Local-first development without a Docker Compose dependency.
- Separate owner and platform-operator security boundaries.

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
| React | 19.3.1 |
| TypeScript | 5.9.3 |
| PostgreSQL | 18.x |

## Current local environment

- macOS with Homebrew.
- PostgreSQL 18.6 installed and running.
- solocrew_dev and solocrew_test databases created.
- solocrew_app database login configured.
- DBeaver connections verified.
- Backend IntelliJ Gradle configurations created.
- Local database credentials are held in an ignored local file.

## Completed slices

- docs/handoffs/SC-FOUNDATION-001-engineering-foundation.md
- docs/handoffs/SC-FOUNDATION-002-backend-foundation.md
- docs/handoffs/SC-FOUNDATION-003-persistence-foundation.md

## Active slice — SC-FOUNDATION-004

Owner Workspace Foundation:

- Next.js Owner Web application.
- React and TypeScript foundation.
- Shared application layout.
- Owner workspace navigation.
- Initial Command Center.
- Responsive desktop and mobile layout.
- Honest empty states rather than fabricated business metrics.
- Frontend type-check and production-build validation.
- Documentation and handoff updates.

This slice does not yet implement authentication,
business APIs, assignment execution or digital employee workflows.

## Development workflow

- Keep main stable.
- Use one feature branch per slice.
- Deliver small, complete vertical slices.
- Provide complete contents for every created or changed file.
- Do not use ZIPs or downloadable archives as implementation handoffs.
- Validate locally before opening a pull request.
- Validate CI before merging.
- Record completed slices in docs/handoffs/.
- Keep this root document focused on current state and next actions.
- Keep architecture decisions in docs/decisions/.

## Immediate next actions

1. Establish the Owner Web application.
2. Install and lock frontend dependencies.
3. Run frontend type-check and production build.
4. Inspect the Command Center in the browser.
5. Validate the repository and CI.
6. Open a pull request and merge after validation.

## Not yet implemented

- Authentication and authorization.
- Organization management.
- Workforce Manager.
- Digital employee execution.
- Personal Research Specialist.
- Assignment creation and tracking.
- Research report generation and review.
- Temporal workflows.
- AI Model Gateway.
- Owner account and workspace management.
- SoloCrew Control Center application.
