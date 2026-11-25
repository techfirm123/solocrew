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
    ./gradlew check

The Gradle bootstrap launcher downloads the pinned distribution
and validates its SHA-256 checksum.

Generate the standard Gradle Wrapper after the first successful
bootstrap, as documented in the local setup guide.

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
