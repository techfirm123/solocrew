# SC-FOUNDATION-005-C2 — Owner Web Access

- Status: Implementation supplied; local validation pending
- Date: 2026-11-26
- Branch: feature/SC-FOUNDATION-005-identity-owner-access

## Objective

Connect the Owner Web application to the validated backend
authentication and owner-session APIs.

Deliver a real authenticated owner experience rather than a
static Command Center preview.

## User experience delivered

- Owner login page.
- Owner registration page.
- Email verification page.
- Verification email resend interaction.
- Protected Owner Command Center.
- Authenticated owner identity in the workspace header.
- Active business workspace information in the workspace header.
- Logout interaction.
- Session-expiry redirect.
- Workspace-unavailable message.

## Technical implementation

### API access

Owner Web uses a same-origin Next.js rewrite:

    /api/backend/:path*
        -> backend /api/v1/:path*

The browser does not call the backend directly.

The browser uses the HttpOnly SOLOCREW_SESSION cookie through
same-origin requests. It never stores session credentials in
localStorage or sessionStorage.

The API client retrieves the CSRF token before every state-changing
request and submits the token using the header name returned by
the backend.

### Server-side session validation

The Command Center is a Server Component.

Before rendering, it forwards the incoming SOLOCREW_SESSION cookie
to:

    GET /api/v1/owner/me

A missing or expired session redirects to:

    /login?reason=session-expired

An authenticated account without an active workspace redirects to:

    /login?reason=workspace-unavailable

The backend remains the authoritative authentication and
authorization boundary. Frontend route handling is not used as
a substitute for backend protection.

### Registration

The registration screen submits:

- email
- displayName
- password
- businessName

Password confirmation is checked in the browser and is not
submitted to the backend.

The registration response remains generic to avoid revealing
whether an email address already has an account.

### Email verification

The verification page accepts the existing email link format:

    /verify-email?token=...

The token is not consumed automatically when the page opens.
The owner must explicitly select the verification action.

The page also allows manual token entry and a verification
email resend request.

After successful verification, the consumed token is removed
from the browser's current address-bar entry.

### Login and logout

Login uses:

    POST /api/v1/public/auth/login

After successful login, the browser fetches:

    GET /api/v1/owner/me

Logout uses:

    POST /api/v1/owner/logout

The server invalidates the persisted session. The browser then
navigates to the sign-in screen.

### Session cookie

The session cookie is named SOLOCREW_SESSION.

It is HttpOnly and uses SameSite=Lax.

Its Secure attribute is controlled by:

    server.servlet.session.cookie.secure

Local development defaults to false. Production configuration
already sets this property to true.

## Files introduced

- apps/owner-web/src/lib/api-client.ts
- apps/owner-web/src/lib/server/owner-session.ts
- apps/owner-web/src/components/auth/auth-shell.tsx
- apps/owner-web/src/components/auth/login-form.tsx
- apps/owner-web/src/components/auth/register-form.tsx
- apps/owner-web/src/components/auth/verification-form.tsx
- apps/owner-web/src/components/auth/logout-button.tsx
- apps/owner-web/src/app/login/page.tsx
- apps/owner-web/src/app/register/page.tsx
- apps/owner-web/src/app/verify-email/page.tsx
- apps/owner-web/src/app/auth.css

## Files changed

- apps/owner-web/next.config.ts
- apps/owner-web/src/components/owner-workspace-shell.tsx
- apps/owner-web/src/app/page.tsx
- apps/owner-web/src/app/layout.tsx
- backend/src/main/java/com/solocrew/platform/security/SessionCookieConfiguration.java
- SOLOCREW_HANDOFF.md

## Local validation

Pending:

- pnpm run check
- backend ./gradlew clean check
- git diff --check
- Browser validation of registration.
- Browser validation of email verification.
- Browser validation of login and session persistence.
- Browser validation of logout and protected route handling.

## Completion criteria

This checkpoint is complete only after the browser journey is
validated against the local PostgreSQL database and Mailpit setup.

Do not merge SC-FOUNDATION-005 until C2 is complete and the
overall identity and owner-access journey passes validation.
