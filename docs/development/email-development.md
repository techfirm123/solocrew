# Local email development

## Local email server

SoloCrew uses Mailpit for local SMTP development.

Mailpit is installed natively on macOS.

Install:

    brew install mailpit

Start:

    brew services start mailpit

Default addresses:

- SMTP: localhost:1025
- Web UI: http://localhost:8025

No Docker or Docker Compose is required.

## Local application configuration

The development Spring profile uses:

- Host: localhost
- Port: 1025
- Authentication: disabled
- STARTTLS: disabled
- Sender: no-reply@solocrew.local

Mailpit accepts these messages and displays them in its local inbox.

No external email is delivered.

## Encryption key

The transactional email outbox encrypts email subjects and bodies
using AES-256-GCM.

Generate a local key:

    openssl rand -hex 32

Add it to:

    backend/local-secrets.properties

Property:

    SOLOCREW_EMAIL_ENCRYPTION_KEY=<64-character-hex-key>

Do not commit local-secrets.properties.

The encryption key must remain available while encrypted outbox
messages are pending. Losing or changing it makes those messages
unreadable.

The automated test profile uses a fixed test-only key. It must never
be used in production.

## SMTP provider changes

The application uses Spring Boot Mail and JavaMailSender.

The email workflow does not depend on Mailpit.

For another SMTP provider, configure:

- SOLOCREW_SMTP_HOST
- SOLOCREW_SMTP_PORT
- SOLOCREW_SMTP_USERNAME
- SOLOCREW_SMTP_PASSWORD
- SOLOCREW_MAIL_FROM
- SOLOCREW_VERIFICATION_URL_BASE
- SOLOCREW_EMAIL_ENCRYPTION_KEY

Production uses authenticated SMTP with STARTTLS.

A provider with a proprietary HTTP API requires a provider adapter.
The identity workflow and email use-case contracts should remain
unchanged.

## Outbox behavior

Email delivery requests are inserted into PostgreSQL as part of
the business transaction.

A scheduled dispatcher:

1. Claims an available outbox row.
2. Decrypts the subject and message body.
3. Sends through JavaMailSender.
4. Marks successful messages as SENT.
5. Retries transient failures.
6. Moves messages to DEAD after five failed attempts.

The outbox is an at-least-once delivery mechanism. A process crash
after SMTP acceptance but before marking a row SENT can result in
a duplicate email on retry.

Verification links remain safe because tokens are single-use.

Successful delivery clears encrypted message payloads from the
outbox row. Delivery metadata remains available for diagnostics.

## Inspecting local messages

Start the backend using the development profile.

Open:

    http://localhost:8025

New registration verification emails should appear in the inbox.

If messages are not appearing, inspect:

- Backend logs.
- The platform.email_outbox table.
- The outbox status and last_error_code.
- Mailpit SMTP readiness.

Do not log or manually share verification tokens.
