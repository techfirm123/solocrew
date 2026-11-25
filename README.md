# SoloCrew

**Your business. Your digital workforce.**

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

The product is designed around business outcomes and accountable
digital employees, not around reproducing a conventional ERP experience.

## Repository layout

    apps/
      owner-web/
      control-center/

    backend/

    docs/
      decisions/
      development/

    scripts/

## Engineering baseline

| Tool | Version |
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

See ADR-0001 for architecture decisions.

See docs/development/local-setup.md for workstation setup.

## Local development

Docker Compose is not required.

### Validate the toolchain

    ./scripts/doctor.sh

### Install frontend workspace dependencies

    pnpm install --frozen-lockfile

### Validate repository conventions

    pnpm run check

### Validate the backend

    cd backend
    ./gradlew clean check

### Start the backend

    ./gradlew bootRun

The development profile is activated by default.

The backend starts on:

    http://localhost:8080

Health endpoints:

    http://localhost:8080/actuator/health
    http://localhost:8080/actuator/health/liveness
    http://localhost:8080/actuator/health/readiness

### Production profile

    SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun

The production profile enables structured JSON logging.

The Gradle Wrapper downloads the pinned distribution and validates
its SHA-256 checksum.

A globally installed Gradle is not required.

## Product principles

- One initial business type.
- Product-led discovery.
- Small, complete vertical slices.
- Modular monolith before microservices.
- Business records remain authoritative.
- AI operates through controlled business capabilities.
- Digital employees have explicit responsibilities.
- Human accountability is preserved.
- Platform administration is separate from business operations.
- No infrastructure dependency without a demonstrated need.
