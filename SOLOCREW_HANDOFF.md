# SoloCrew — Current Project Handoff

Last updated: 2026-11-26

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
| SC-FOUNDATION-005-C2 — Owner Web Authentication & Access | In progress |

Current branch:

    feature/SC-FOUNDATION-005-identity-owner-access

## Product context

SoloCrew is an AI-powered digital workforce platform for an
independently operated computer sales and service business.

The product is not intended to become another conventional ERP.

The primary owner experience is the Owner Workspace. It will let
the owner direct digital employees, follow their work, review
deliverables and remain in control of important decisions.

The SoloCrew Control Center is a separate platform-operator
experience.

## Architecture direction

- Modular monolith initially.
- Java 21 and Spring Boot backend.
- Next.js, React and TypeScript web applications.
- PostgreSQL as authoritative relational storage.
- Flyway for versioned database changes.
- Spring Security for authentication and authorization.
- Spring Session JDBC for server-side sessions.
- Transactional email outbox.
- Provider-independent SMTP delivery.
- Append-oriented audit events.
- Correlation IDs for request and execution diagnostics.
- Temporal for durable workflow execution.
- Spring AI as an AI integration layer, not the application
  orchestrator.
- No local Docker Compose dependency.

Architecture decisions:

- `docs/decisions/ADR-0001-engineering-baseline.md`
- `docs/decisions/ADR-0002-identity-access-and-auditability.md`

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

## Local development environment

- macOS with Homebrew.
- PostgreSQL 18.6 installed and running natively.
- `solocrew_dev` and `solocrew_test` databases.
- `solocrew_app` application database role.
- DBeaver connections verified.
- IntelliJ Gradle configurations for the backend.
- Local secrets stored in the ignored
  `backend/local-secrets.properties` file.
- Mailpit installed and running natively.
- Mailpit SMTP: `localhost:1025`.
- Mailpit web UI: `http://localhost:8025`.
- Docker Compose is not required for local development.

Do not put local credentials or encryption keys into tracked files.

## Completed handoffs

- `docs/handoffs/SC-FOUNDATION-001-engineering-foundation.md`
- `docs/handoffs/SC-FOUNDATION-002-backend-foundation.md`
- `docs/handoffs/SC-FOUNDATION-003-persistence-foundation.md`
- `docs/handoffs/SC-FOUNDATION-004-owner-workspace-command-center.md`
- `docs/handoffs/SC-FOUNDATION-005-C1-owner-login-protected-workspace.md`

## Identity & Owner Access — completed checkpoints

### 005-A — Identity & Security Foundation

- User identity schema.
- Business workspace schema.
- Workspace membership schema.
- BCrypt password encoder.
- Database-backed owner UserDetailsService.
- Spring Security filter chain.
- CSRF protection.
- PostgreSQL-backed Spring Session tables.
- Append-oriented audit event schema and writer.
- Audit mutation protection.
- Correlation ID filter.
- Security, session, identity and audit tests.

Migrations V1–V3 were applied and verified in both local databases.

### 005-B — Owner Registration & Email Verification

- Owner registration request and response contracts.
- Owner and business workspace provisioning.
- Owner membership provisioning.
- Verification token generation and SHA-256 token hashing.
- 24-hour token expiration.
- Verification token consumption.
- Transactional email outbox.
- AES-256-GCM encryption for sensitive email payloads.
- SMTP delivery through JavaMailSender.
- Scheduled outbox dispatcher.
- Retry and dead-letter states.
- Mailpit development configuration.
- Public request rate limiting.
- Email verification and resend APIs.
- Registration, verification and resend audit events.

Migration V4 creates the email verification, email outbox and
public request rate-limit tables.

The real registration and verification flow was exercised locally
through Mailpit. The registered user became active, the outbox
message was sent and cleared, the token was consumed, and the
corresponding audit events were recorded.

### 005-C1 — Backend Owner Login & Protected Workspace

- Owner login and logout APIs.
- Server-side session creation and invalidation.
- Session ID rotation after authentication.
- CSRF token delivery.
- `SOLOCREW_SESSION` HttpOnly cookie.
- Authenticated owner and active workspace context.
- Active user, membership and workspace checks.
- Login rate limiting.
- Login and logout audit events.
- Authentication and session integration tests.

The backend checkpoint was locally validated and pushed to the
feature branch.

## Active checkpoint — 005-C2 Owner Web Authentication & Access

### Current routes

Public routes:

- `/` — public SoloCrew landing page.
- `/login` — owner sign-in.
- `/register` — owner registration.
- `/verify-email` — automatic email-link verification and recovery.

Protected route:

- `/workspace` — Owner Command Center.

The public root route must never require authentication.

The protected workspace route must redirect unauthenticated users
to the login page.

Successful login must navigate to `/workspace`.

### Owner journey

The intended registration and access flow is:

1. Owner visits the public landing page.
2. Owner creates an account.
3. SoloCrew sends a verification email.
4. Owner opens the verification link.
5. Verification page extracts and submits the token automatically.
6. Backend validates and consumes the token.
7. Owner sees verification success.
8. Owner signs in.
9. Owner reaches `/workspace`.
10. Owner can sign out and return to the public experience.

Manual token copy-and-paste is not part of the intended experience.

The resend-email form remains available for expired or invalid links.

The owner has visually reviewed the landing page, registration,
verification, login and workspace experience. Screenshots confirmed
the automatic verification success screen and the `/workspace`
destination after login.

Automated validation of the latest UI and design-token changes
must be confirmed before C2 is closed.

### Theme-ready design foundation

Current provisional theme:

    midnight-botanical

The theme uses deep pine, warm ivory, moss green and a restrained
citrus-lime highlight.

A semantic token foundation is being introduced:

    apps/owner-web/src/app/design-tokens.css
    docs/development/design-system.md

The root HTML element identifies the active theme through:

    data-theme="midnight-botanical"

New reusable UI components must use semantic design tokens rather
than hardcoded palette values.

Compatibility aliases are temporary. Existing CSS colour values
must be progressively migrated before user-selectable themes are
released.

### C2 remaining work

1. Apply and validate the theme-ready token foundation.
2. Run the repository checks and inspect the resulting changes.
3. Review authentication, public site and workspace styling for
   consistency.
4. Verify registration, automatic email verification, login,
   protected workspace access and logout.
5. Confirm the missing 004 and C1 handoff files are present.
6. Complete the C2 handoff.
7. Commit and push the validated feature branch.

## Future product milestone — Workspace Appearance

SC-UX-APPEARANCE-001 — Workspace Appearance & Personalization.

Planned capabilities:

- Curated palette library.
- Theme previews.
- Personal theme selection.
- Server-side persistence of user theme preference.
- Consistent theme application across Owner Web.
- Accessibility validation.

Personal theme preference and workspace-specific branding must
remain separate concepts.

This capability is deliberately not implemented in the current
foundation.

## Development conventions

- Keep `main` stable.
- Use one feature branch per slice.
- Build small, complete vertical slices.
- Validate each slice before moving forward.
- Provide complete contents for created or changed files in the
  implementation handoff.
- Do not use ZIP files or downloadable archives as the code handoff.
- Do not assume Docker Compose is available.
- Do not claim tests passed until they have actually been run.
- Record each completed slice in `docs/handoffs/`.
- Keep this root document focused on current state and next actions.
- Keep architecture decisions in `docs/decisions/`.
- New reusable UI components must use semantic design tokens.
- Do not introduce hardcoded brand colours in new components.

## Not yet implemented

- Password recovery.
- Platform operator authentication and MFA.
- Personal Research Specialist execution.
- Workforce Manager and specialist orchestration.
- Work assignment and approval workflows.
- Temporal workflows.
- AI Model Gateway.
- Business operational capabilities.
- SoloCrew Control Center.
- Mobile applications.
