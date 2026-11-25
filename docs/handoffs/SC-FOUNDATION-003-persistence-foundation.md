# SC-FOUNDATION-003 — Persistence Foundation

- Status: Complete
- Branch: feature/SC-FOUNDATION-003-persistence-foundation
- Target branch: main
- Merged: 2026-11-25

## Objective

Establish PostgreSQL connectivity, versioned database migrations,
transaction support and repeatable persistence integration tests.

## Delivered

- PostgreSQL JDBC driver integration.
- Spring Boot JDBC configuration.
- Hikari connection pooling.
- Flyway migration management.
- PostgreSQL-specific Flyway database module.
- Initial platform schema.
- Dedicated development and test databases.
- JDBC connectivity integration tests.
- Flyway migration integration tests.
- Spring-managed transaction integration test.
- PostgreSQL-backed GitHub Actions validation.
- Local PostgreSQL development instructions.
- DBeaver connection instructions.
- Local secrets configuration.
- IntelliJ Gradle run configurations.

## Local database setup

PostgreSQL 18.6 was installed and started natively on macOS.

Databases:

| Database | Owner |
|---|---|
| solocrew_dev | solocrew_app |
| solocrew_test | solocrew_app |

The application role has LOGIN enabled.

Both databases were successfully accessed using the application role.

## Migration validation

Migration:

    V1__create_platform_schema.sql

The migration creates the initial platform schema.

It intentionally introduces no business tables.

Flyway successfully applied migration version 1 in both databases.

Development database:

- Version: 1
- Description: create platform schema
- Success: true
- Execution time: 6 ms

Test database:

- Version: 1
- Description: create platform schema
- Success: true
- Execution time: 3 ms

## Automated validation

- Backend Gradle tests passed.
- PostgreSQL integration tests passed.
- Flyway migration tests passed.
- Transaction integration test passed.
- Repository validation passed.
- GitHub Actions validation passed.
- Pull request was merged into main.

## Architecture conventions established

- PostgreSQL is the authoritative relational data store.
- Flyway is the source of truth for database schema changes.
- Applied migrations must not be edited.
- Schema changes are introduced through new versioned migrations.
- DBeaver is used for inspection and controlled troubleshooting.
- Application tables must not be created manually in DBeaver.
- Development and test databases remain separate.
- Local development does not require Docker Compose.

## Next slice

SC-FOUNDATION-004 — Owner Workspace Foundation.

The next slice establishes the Next.js Owner Web application,
navigation and the initial Owner Command Center.

The Personal Research Specialist will be introduced into this
workspace through a subsequent vertical slice.
