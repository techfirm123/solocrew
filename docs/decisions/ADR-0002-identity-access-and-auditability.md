# ADR-0002: Identity, Owner Access and Auditability

- Status: Accepted
- Date: 2026-11-25

## Context

SoloCrew is a multi-business digital workforce platform.

Business owners require secure access to their workspaces.
Digital employee assignments and reports must be attributable
to the correct business and owner.

Platform maintainers require operational visibility without
unrestricted access to customer business information.

The product requires email verification, secure sessions,
business-level authorization, audit history and operational
diagnostics.

## Identity

- Maintain an application-owned user identity.
- Use email as the initial login identifier.
- Use Spring Security for authentication and authorization.
- Use BCrypt for adaptive one-way password hashing.
- Keep user identity separate from business workspace records.
- Keep internal user IDs independent of authentication provider IDs.
- Preserve the ability to introduce OIDC federation later.
- Do not introduce a separate identity-provider service initially.

## Session management

- Use server-side sessions.
- Persist sessions in PostgreSQL using Spring Session JDBC.
- Use HttpOnly session cookies.
- Use Secure cookies in production.
- Use SameSite=Lax as the initial cookie policy.
- Protect browser state-changing requests with CSRF protection.
- Rotate the session identifier during authentication.
- Invalidate the session on logout.
- Do not store session credentials in browser localStorage.
- Use explicit session expiration and revocation policies.

## Business workspace and authorization

- Users are independent identities.
- A user may belong to one or more business workspaces.
- Workspace membership determines business roles.
- Initial roles are OWNER, ADMIN and MEMBER.
- Backend authorization derives workspace access from the
  authenticated principal and membership records.
- Client-supplied workspace IDs are never trusted as authorization.
- Platform operators are separate principals from business users.

## Email delivery

- Use Spring Boot Mail and JavaMailSender.
- Use standard SMTP as the provider integration boundary.
- Use Mailpit for local development.
- Keep email templates and business workflows provider-independent.
- Persist email delivery requests through a transactional outbox.
- Retry transient delivery failures.
- Store verification-token hashes rather than raw tokens.
- Never log verification tokens or complete verification URLs.

SMTP-compatible providers should be replaceable through configuration.
A provider offering only a proprietary HTTP API requires a provider
adapter but must not change the email use-case contract.

## Email verification

- New owner accounts begin in PENDING_EMAIL_VERIFICATION.
- Verification tokens are cryptographically random.
- Only token hashes are stored.
- Tokens are single-use and expire.
- Resend requests are rate-limited.
- Account activation happens only after successful verification.
- Verification outcomes are auditable.
- Public endpoints must not expose unrelated account existence.

## Auditability

Maintain a dedicated append-oriented audit event store.

Audit events identify:

- Event ID and timestamp.
- Business workspace when applicable.
- Actor type and actor ID.
- Event type and action.
- Resource type and resource ID.
- Outcome.
- Correlation ID.
- Sanitized metadata.

Actor types:

- USER
- DIGITAL_EMPLOYEE
- SYSTEM
- PLATFORM_OPERATOR

Important events include:

- Registration and verification.
- Authentication success and failure.
- Logout and session revocation.
- Membership and permission changes.
- Assignment lifecycle events.
- Digital employee execution.
- Human approvals and rejections.
- Support access and diagnostic sharing.

Audit records must not contain passwords, session secrets,
verification tokens, API keys or unrestricted private content.

The application exposes no audit-event update or deletion operations.
Database triggers reject ordinary UPDATE, DELETE and TRUNCATE
operations against the audit-event table.

Production must separate migration-owner and runtime database
privileges. Append-only triggers are not a substitute for
database-level privilege separation.

## Operational observability

- Generate or validate a correlation ID for every HTTP request.
- Return the correlation ID in the response header.
- Include correlation IDs in logs and audit events.
- Trace important business workflows using stable execution IDs.
- Record failures with sanitized error categories.
- Capture timings and external provider outcomes.
- Do not log credentials, tokens or private chain-of-thought.
- Keep operational logs separate from durable audit records.

## Support diagnostics

Customer diagnostic packages are a later support capability.

A package may include:

- Application version and environment.
- Relevant audit events.
- Correlation and execution IDs.
- Sanitized error details.
- Relevant integration status and provider error codes.

Business documents, credentials, tokens and private content are
excluded by default.

The customer must preview and explicitly authorize the information
shared with SoloCrew support.

Exceptional support access must be scoped, time-limited,
customer-authorized and audited.

## Security principles

- Deny access by default.
- Enforce authorization in the backend.
- Never rely on frontend visibility for security.
- Protect against CSRF and session fixation.
- Rate-limit authentication and verification operations.
- Avoid account and email enumeration.
- Separate platform administration from business operations.
- Minimize sensitive information in logs and diagnostics.
- Preserve traceability without unnecessary data collection.

## Consequences

- Spring Security is part of the backend foundation.
- Spring Session JDBC persists authenticated sessions.
- PostgreSQL stores identities, workspaces, memberships and audit events.
- Mailpit provides local SMTP testing.
- Owner Web routes require authenticated workspace context.
- Digital employee execution must be attributable to an actor
  and business workspace.
- The Control Center consumes audited operational data through
  a separate authorization boundary.

## Validation required

- Owner registration and verification.
- Unverified account access restrictions.
- Secure login and logout.
- Session persistence and invalidation.
- CSRF protection.
- Workspace isolation.
- Email outbox retry behavior.
- Audit event creation for security events.
- Correlation ID propagation.
- Sensitive-data exclusion from logs.
- Customer-authorized diagnostic boundaries.

This ADR defines architectural requirements. It does not claim
that capabilities outside the current implementation checkpoint
have already been delivered.
