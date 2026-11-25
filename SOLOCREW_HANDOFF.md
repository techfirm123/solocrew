# SoloCrew handoff

- Current slice: SC-FOUNDATION-002
- Title: Backend Application Foundation
- Date: 2026-11-25
- Status: Implementation prepared; local validation pending

## Product context

SoloCrew is an AI-powered digital workforce for a single-person
computer sales and service business.

The product is not intended to be another ERP application.

The owner interacts with a digital workforce through the Owner
Command Center.

SoloCrew operators manage the platform through the separate
SoloCrew Control Center.

## Delivered in this slice

- Spring Boot 4.2.0 application.
- Java 21 Gradle build.
- Spring Modulith 2.2.0 integration.
- Platform module boundary.
- Shared UTC Clock configuration.
- Runtime module verification.
- Static module architecture test.
- Actuator health endpoint.
- Liveness and readiness endpoints.
- Development profile.
- Test profile.
- Production profile.
- Production structured JSON logging.
- Application startup and health endpoint tests.

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

## Validation required

Run from the backend directory:

    ./gradlew clean check

Start the backend:

    ./gradlew bootRun

Validate health:

    curl http://localhost:8080/actuator/health

Validate liveness:

    curl http://localhost:8080/actuator/health/liveness

Validate readiness:

    curl http://localhost:8080/actuator/health/readiness

Run from the repository root:

    pnpm run check

Do not merge this slice until local validation succeeds.

The standard Gradle Wrapper must also be generated and committed.

## Not implemented

- PostgreSQL integration.
- Flyway migrations.
- Authentication and authorization.
- Organization management.
- Workforce orchestration.
- Business capabilities.
- Temporal integration.
- AI Model Gateway.
- Personal Research Specialist.
- Owner Web application.
- SoloCrew Control Center application.

## Next slice

SC-FOUNDATION-003 — Local Persistence Foundation.

Expected scope:

- PostgreSQL 18 local development setup.
- Spring Boot database integration.
- Flyway migration foundation.
- Database connection configuration by profile.
- Persistence integration tests.
- No Docker Compose dependency.

The first Personal Research Specialist will follow once the application
and persistence foundations are validated.

## Development conventions

- Deliver complete vertical slices.
- Provide complete contents for every created or changed file.
- Do not use downloadable ZIPs as implementation handoffs.
- Validate each slice before proceeding.
- Update this handoff after each completed slice.
- Keep main stable.
- Use one feature branch per slice.
