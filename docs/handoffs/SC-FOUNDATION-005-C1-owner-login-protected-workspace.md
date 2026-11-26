# SC-FOUNDATION-005-C1 — Backend Owner Login & Protected Workspace

- Status: Complete
- Parent slice: SC-FOUNDATION-005 — Identity & Owner Access
- Branch: feature/SC-FOUNDATION-005-identity-owner-access
- Target: main

## Objective

Provide secure owner authentication, durable server-side sessions,
authenticated owner context and protected workspace APIs.

This checkpoint builds on the identity, registration and email
verification foundation delivered in 005-A and 005-B.

## Delivered

### Owner authentication

- Owner login endpoint.
- Credential authentication through Spring Security.
- BCrypt password verification.
- Email normalization before authentication.
- Rejection of unverified or inactive accounts.
- Rejection when no active business workspace is available.
- Session ID rotation after authentication.
- Explicit persistence of the authenticated SecurityContext.

### Session lifecycle

- PostgreSQL-backed Spring Session JDBC.
- Dedicated `SOLOCREW_SESSION` cookie.
- HttpOnly cookie.
- Path `/`.
- SameSite `Lax`.
- Secure-cookie behaviour follows the HTTPS request in the current
  configuration.
- Server-side logout and session invalidation.
- SecurityContext cleanup on logout.

### CSRF

- CSRF protection remains enabled.
- CSRF token retrieval endpoint.
- Login and logout remain CSRF-protected.
- Session authentication rotates the session and CSRF state.

### Owner workspace access

- Authenticated owner context endpoint.
- Active user validation.
- Active membership validation.
- Active workspace validation.
- Owner workspace role and identity returned to the client.
- Workspace context resolved from persisted membership data rather
  than trusted from browser-supplied values.

### Rate limiting and auditability

- Login attempts rate-limited by IP address.
- Login attempts rate-limited by normalized account email.
- Successful login audit event.
- Failed login audit event.
- Rate-limited login audit event.
- Login denied when no active workspace is available.
- Logout audit event.

## API contracts

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/public/csrf` | Obtain CSRF token |
| POST | `/api/v1/public/auth/login` | Authenticate owner |
| GET | `/api/v1/owner/me` | Retrieve authenticated owner and workspace |
| POST | `/api/v1/owner/logout` | Invalidate owner session |

### Login outcomes

- `200 OK` — authenticated.
- `401 Unauthorized` — invalid credentials or account not eligible
  to authenticate.
- `403 Forbidden` — no active business workspace.
- `429 Too Many Requests` — login rate limit reached.

### Protected owner context

The owner context includes:

- User ID.
- Email.
- Display name.
- Workspace ID.
- Workspace name.
- Workspace slug.
- Membership role.

## Important implementation locations

    backend/src/main/java/com/solocrew/platform/identity/
    backend/src/main/java/com/solocrew/platform/security/
    backend/src/test/java/com/solocrew/platform/identity/
    backend/src/test/java/com/solocrew/platform/security/

Key classes include:

- OwnerAuthenticationController
- OwnerAuthenticationService
- OwnerAuthenticationOutcome
- OwnerSessionController
- OwnerWorkspaceAccessService
- OwnerSessionResponse
- OwnerLoginRequest
- OwnerUserPrincipal
- OwnerUserDetailsService
- SecurityConfiguration
- SessionCookieConfiguration
- CsrfTokenController

## Automated validation

The owner authentication integration tests cover:

- Successful login.
- Session ID rotation.
- Persisted authenticated session.
- Authenticated owner context retrieval.
- Successful login audit event.
- Incorrect password rejection.
- Unverified account rejection.
- CSRF enforcement.
- Logout and session invalidation.
- Logout audit event.
- Principal credential erasure.

The checkpoint was locally validated. Backend Gradle checks,
repository frontend checks and Git diff validation passed.
The implementation was pushed to the feature branch.

## Security boundaries

- The browser does not choose its own authenticated workspace.
- The backend resolves workspace access from active membership.
- A successful credential match alone is insufficient when the
  account is unverified, inactive or lacks an active workspace.
- CSRF protection is not disabled to simplify browser integration.
- The session remains server-side and is invalidated on logout.

## Not included

- Password recovery.
- MFA.
- Platform operator authentication.
- Owner Web authentication UI.
- Public marketing landing page.
- Multi-workspace switching.
- Advanced session management and device management.

Owner Web integration is handled in SC-FOUNDATION-005-C2.
