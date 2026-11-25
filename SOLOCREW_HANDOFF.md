# SoloCrew handoff

- Current slice: SC-FOUNDATION-001
- Title: Repository and Engineering Setup
- Date: 2026-11-25
- Status: Scaffold created; local validation pending

## Product context

SoloCrew is an AI-powered digital workforce for a single-person
computer sales and service business.

The product is not intended to be another ERP application.

The owner interacts with a digital workforce through the Owner
Command Center.

SoloCrew operators manage the platform through the separate
SoloCrew Control Center.

## Delivered

- Monorepo structure.
- pnpm workspace configuration.
- Owner Web workspace placeholder.
- Control Center workspace placeholder.
- Java 21 Gradle backend baseline.
- Gradle 9.9.2 bootstrap launcher.
- Gradle distribution checksum configuration.
- Git conventions.
- Editor configuration.
- Toolchain doctor script.
- Whitespace validation.
- GitHub Actions CI foundation.
- ADR-0001.
- Local setup documentation.

## Selected toolchain

| Component | Version |
|---|---|
| Java | 21 LTS |
| Gradle | 9.9.2 |
| Node.js | 24 LTS |
| pnpm | 11.29.0 |
| Spring Boot | 4.2.0 |
| Spring Modulith | 2.2.0 |
| Spring AI | 2.1.0 |
| Next.js | 16.4.6 |
| PostgreSQL | 18.x |

## Local validation

Run:

    ./scripts/doctor.sh

    pnpm install --frozen-lockfile

    pnpm run check

    cd backend

    ./gradlew check

Generate and verify the standard Gradle Wrapper as documented in:

    docs/development/local-setup.md

## Not implemented

- Spring Boot application.
- Business domain modules.
- PostgreSQL integration.
- Flyway migrations.
- Authentication and authorization.
- Next.js applications.
- Temporal workflow integration.
- AI Model Gateway.

## Next slice

SC-FOUNDATION-002 — Backend Application Foundation.

Scope:

- Spring Boot 4.2 application.
- Java 21.
- Initial application configuration.
- Spring Modulith integration.
- Module boundary verification.
- Application profiles.
- Health endpoints.
- Backend build validation.

## Development conventions

- Deliver complete vertical slices.
- Provide complete contents for every created or changed file.
- Do not use downloadable ZIPs as implementation handoffs.
- Validate each slice before proceeding.
- Update this handoff after each completed slice.
