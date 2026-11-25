# ADR-0001: SoloCrew engineering baseline

- Status: Accepted
- Date: 2026-11-25
- Decision owners: SoloCrew product and engineering

## Context

SoloCrew is an AI-powered digital workforce for an independently
operated computer sales and service business.

The product requires reliable business transactions, durable background
execution, controlled AI capabilities, separate owner and platform
operator experiences, and an efficient local development workflow.

The initial architecture is a modular monolith.

Independent services and infrastructure will only be introduced
when actual product or operational requirements justify them.

## Decisions

### Backend

- Java 21 LTS.
- Spring Boot 4.2.0.
- Gradle 9.9.2.
- Spring Modulith 2.2.0.
- Spring AI 2.1.0.
- Temporal Java SDK for durable workflow orchestration.

### Web

- Node.js 24 LTS.
- pnpm 11.29.0.
- Next.js 16.4.6.
- React and TypeScript.

Two independent web applications:

- SoloCrew Owner Workspace.
- SoloCrew Control Center.

### Mobile

- React Native.
- TypeScript.
- Android-first implementation.
- Future iOS compatibility.

### Data and operations

- PostgreSQL 18.x.
- Flyway for database migrations.
- OpenAPI for backend/frontend contracts.
- OIDC-compatible identity integration.
- Structured logs, metrics and traces.

### Repository and local development

- One monorepo.
- pnpm workspaces for frontend applications.
- Independent Gradle backend build.
- Docker Compose is not a local-development prerequisite.
- Gradle distribution checksum verification.
- Java 21 and Node.js 24 are the mandatory local runtimes.

## Rationale

Java 21 is an established LTS runtime that supports the selected
Spring Boot generation and provides a stable foundation for business
domains, transactions, authorization and background execution.

Node.js 24 is an LTS release compatible with the selected Next.js
and pnpm versions.

We prefer stable LTS runtimes over adopting newer major versions
without a demonstrated product requirement.

Spring AI provides model integration without making an LLM framework
the application orchestrator.

Temporal provides durable workflows that can pause for human decisions
and recover after worker interruptions.

PostgreSQL provides the initial relational foundation without requiring
a separate database for each context type.

## Consequences

- The backend targets Java 21.
- All backend compilation targets Java 21 bytecode.
- Local development uses Node.js 24.
- CI validates against Java 21 and Node.js 24.
- The backend remains a modular monolith.
- API and workflow-worker processes may use the same application artifact.
- Business records remain authoritative and independent of AI-provider state.
- AI models cannot access the database directly.
- Owner and platform-operator experiences have separate security boundaries.

## Validation required

The foundation implementation must verify:

- Java 21 and Gradle compatibility.
- Spring Boot and Spring AI dependency compatibility.
- Node.js 24 and pnpm compatibility.
- Frontend build compatibility.
- Database integration.
- Temporal workflow recovery.
- Business-data isolation.

This ADR records the selected baseline, not a claim that every
integration has already been validated.

Changes must be documented when compatibility, security or actual
product needs justify revisiting the decision.
