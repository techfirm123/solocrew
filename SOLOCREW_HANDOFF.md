# SoloCrew — Current Project Handoff

Last updated: 2026-11-25

## Current status

- Complete: SC-FOUNDATION-001 — Engineering Foundation
- Complete: SC-FOUNDATION-002 — Backend Application Foundation
- Complete: SC-FOUNDATION-003 — Persistence Foundation
- Complete: SC-FOUNDATION-004 — Owner Workspace Foundation
- In progress: SC-FOUNDATION-005 — Identity & Owner Access

Current branch:

    feature/SC-FOUNDATION-005-identity-owner-access

Current checkpoint:

    SC-FOUNDATION-005-B — Owner Registration & Email Verification

## Product context

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

The product is not intended to become another conventional ERP.

The Owner Command Center is the primary owner experience.

The SoloCrew Control Center is a separate platform-operator experience.

## Architecture direction

- Modular monolith initially.
- Java 21 and Spring Boot backend.
- Next.js, React and TypeScript web applications.
- PostgreSQL as authoritative relational storage.
- Flyway for versioned database changes.
- JDBC for direct persistence access.
- Spring Security for authentication and authorization.
- Spring Session JDBC for server-side sessions.
- SMTP abstraction for provider-independent email delivery.
- Mailpit for local email development.
- AES-256-GCM encryption for sensitive email outbox payloads.
- PostgreSQL-backed transactional email outbox.
- Append-oriented audit events.
- Correlation IDs for request and execution diagnostics.
- Temporal for durable workflow execution.
- Spring AI as an AI integration layer, not the application orchestrator.
- No local Docker Compose dependency.

Architecture decisions:

- docs/decisions/ADR-0001-engineering-baseline.md
- docs/decisions/ADR-0002-identity-access-and-auditability.md

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
- PostgreSQL 18.6 running natively.
- solocrew_dev and solocrew_test databases.
- solocrew_app database login.
- DBeaver connections verified.
- Backend IntelliJ Gradle configurations.
- Local secrets in an ignored properties file.
- Mailpit installed and running.
- Mailpit SMTP: localhost:1025.
- Mailpit web UI: http://localhost:8025.

## Completed slices

- docs/handoffs/SC-FOUNDATION-001-engineering-foundation.md
- docs/handoffs/SC-FOUNDATION-002-backend-foundation.md
- docs/handoffs/SC-FOUNDATION-003-persistence-foundation.md

SC-FOUNDATION-004 is merged. Owner Web source code is under
apps/owner-web/.

## Active slice — SC-FOUNDATION-005

Identity & Owner Access.

### Checkpoint 005-A — Identity & Security Foundation

Implementation and local validation completed:

- User identity schema.
- Business workspace schema.
- Workspace membership schema.
- Database-backed UserDetailsService.
- BCrypt password encoder.
- Spring Security filter chain.
- CSRF protection.
- PostgreSQL-backed Spring Session tables.
- Append-only audit-event schema and writer.
- Audit mutation protection.
- Correlation ID filter.
- Security, session, identity and audit integration tests.

Migrations V1, V2 and V3 were verified in both local databases.

### Checkpoint 005-B — Owner Registration & Email Verification

Current implementation:

- Registration request and response contracts.
- Owner registration service.
- Business workspace provisioning.
- Owner membership provisioning.
- Verification token generation and SHA-256 hashing.
- 24-hour verification expiration.
- Transactional email outbox.
- AES-256-GCM email content encryption.
- Standard SMTP delivery through JavaMailSender.
- Scheduled outbox dispatcher.
- Retry and dead-letter states.
- Mailpit development configuration.
- Public endpoint rate limiting.
- Email verification endpoint.
- Verification resend endpoint.
- Registration, verification and resend audit events.
- Integration tests.

### Checkpoint 005-C — Owner Login & Protected Workspace

Planned:

- Login and logout APIs.
- Session creation and invalidation.
- CSRF token delivery to Owner Web.
- Owner registration and verification screens.
- Owner login screen.
- Protected Command Center routes.
- Authenticated owner identity display.
- Workspace membership authorization.
- Security and session lifecycle tests.

## Immediate next actions

1. Add the generated email encryption key to the local secrets file.
2. Install the updated backend dependencies.
3. Apply Flyway migration V4 to development and test databases.
4. Run the new encryption, registration and verification tests.
5. Start the backend and submit a registration.
6. Inspect the verification email in Mailpit.
7. Verify successful activation.
8. Validate the backend, frontend and CI.
9. Continue to checkpoint 005-C after B passes.

## Development conventions

- Keep main stable.
- Use one feature branch per slice.
- Deliver small, complete vertical slices.
- Provide complete contents for all created or changed files.
- Do not use ZIPs or downloadable archives as implementation handoffs.
- Validate locally before opening a pull request.
- Validate CI before merging.
- Record completed slices in docs/handoffs/.
- Keep this root document focused on current state and next actions.
- Keep architecture decisions in docs/decisions/.

## Not yet implemented

- Owner login and logout.
- Protected Owner Web routes.
- Password recovery.
- Platform operator authentication.
- Personal Research Specialist.
- Workforce Manager.
- Temporal workflows.
- AI Model Gateway.
- SoloCrew Control Center.
- Customer diagnostic package sharing.
