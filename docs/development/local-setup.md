# Local development setup

## Required tools

| Tool | Required version |
|---|---:|
| Git | Current supported release |
| Java JDK | 21 LTS |
| Node.js | 24 LTS |
| pnpm | 11.29.0 |
| PostgreSQL | 18.x |
| curl and unzip | Required by Gradle bootstrap |

Docker and Docker Compose are not required for local development.

## macOS setup

### 1. Java

Install a JDK 21 distribution.

Verify:

    java -version

Configure JAVA_HOME if necessary:

    export JAVA_HOME=$(/usr/libexec/java_home -v 21)

### 2. Node.js

Use your preferred Node version manager.

With nvm:

    nvm install 24
    nvm use 24

Verify:

    node --version
    npm --version

The repository contains .nvmrc and .node-version files to identify
the required Node.js major version.

### 3. pnpm

Install the pinned package manager:

    npm install --global pnpm@11.29.0

Verify:

    pnpm --version

### 4. PostgreSQL 18

Install PostgreSQL using Homebrew:

    brew install postgresql@18

Start the PostgreSQL service:

    brew services start postgresql@18

Add its command-line tools to your PATH:

    echo 'export PATH="$(brew --prefix postgresql@18)/bin:$PATH"' >> ~/.zshrc

Reload the shell:

    source ~/.zshrc

Verify the installed version:

    psql --version

Check server readiness:

    pg_isready -h localhost -p 5432

Expected result:

    localhost:5432 - accepting connections

### 5. Create the SoloCrew databases

Connect to the PostgreSQL maintenance database:

    psql -d postgres

Create the application login if it does not already exist:

    CREATE ROLE solocrew_app LOGIN;

Set its password:

    \password solocrew_app

Create the development and test databases if they do not already exist:

    CREATE DATABASE solocrew_dev OWNER solocrew_app;
    CREATE DATABASE solocrew_test OWNER solocrew_app;

Verify:

    \l

Exit:

    \q

These are one-time setup commands. Do not execute CREATE ROLE
or CREATE DATABASE again if the objects already exist.

For an existing role, verify and enable login if required:

    ALTER ROLE solocrew_app LOGIN;

The application role owns both databases. It does not need PostgreSQL
superuser privileges.

### 6. Verify database connectivity

Development database:

    psql -h localhost -U solocrew_app -d solocrew_dev \
      -c "SELECT current_database(), current_user, version();"

Test database:

    psql -h localhost -U solocrew_app -d solocrew_test \
      -c "SELECT current_database(), current_user, version();"

Both commands should connect using the application user's password.

### 7. Configure DBeaver

Create two PostgreSQL connections.

Development:

| Setting | Value |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | solocrew_dev |
| Username | solocrew_app |
| Password | Local database password |

Tests:

| Setting | Value |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | solocrew_test |
| Username | solocrew_app |
| Password | Local database password |

Use DBeaver to inspect schemas, tables and Flyway migration history.

Do not create application tables manually in DBeaver.
All application schema changes must be introduced through
version-controlled Flyway migrations.

### 8. Configure local database secrets

Create a local copy of the example file:

    cp backend/local-secrets.properties.example \
       backend/local-secrets.properties

Restrict access to the file:

    chmod 600 backend/local-secrets.properties

Edit backend/local-secrets.properties:

    SOLOCREW_DATABASE_PASSWORD=your-actual-local-password

Do not commit this file.

The file is excluded by .gitignore.

The development and test Spring profiles import this local file
automatically. The backend can therefore run through IntelliJ,
Gradle or the terminal without repeatedly exporting the password.

The default local database settings are:

Development:

    jdbc:postgresql://localhost:5432/solocrew_dev

Tests:

    jdbc:postgresql://localhost:5432/solocrew_test

The database URL and username can be overridden using:

- SOLOCREW_DATABASE_URL
- SOLOCREW_DATABASE_USERNAME

Production credentials must be configured explicitly.

## Install workspace dependencies

From the repository root:

    pnpm install --frozen-lockfile

When adding or changing frontend dependencies, run:

    pnpm install

Commit the resulting pnpm-lock.yaml.

## Backend development

Navigate to the backend directory:

    cd backend

Check the Gradle version:

    ./gradlew --version

The standard Gradle Wrapper downloads the pinned Gradle 9.9.2
distribution on first use.

A globally installed Gradle is not required.

### Validate the backend

From the backend directory:

    ./gradlew clean check

The test profile connects to solocrew_test.

### Run the backend

    ./gradlew bootRun

The development profile connects to solocrew_dev.

The backend starts on:

    http://localhost:8080

Health endpoints:

    http://localhost:8080/actuator/health
    http://localhost:8080/actuator/health/liveness
    http://localhost:8080/actuator/health/readiness

## Owner Web development

From the repository root:

    pnpm run dev:owner-web

The Owner Web application starts on:

    http://localhost:3000

The application is located at:

    apps/owner-web/

### Validate the Owner Web application

Run:

    pnpm --filter @solocrew/owner-web typecheck

Build for production:

    pnpm --filter @solocrew/owner-web build

Or run the complete repository check:

    pnpm run check

The repository check includes workspace validation, whitespace
validation, Owner Web type-checking and the Owner Web production build.

## IntelliJ IDEA

### Backend development configuration

Use the Gradle run configuration:

    SoloCrew Backend - Development

Task:

    bootRun

This starts the backend against solocrew_dev.

### Backend test configuration

Use:

    SoloCrew Backend - Tests

Task:

    clean check

This runs backend validation against solocrew_test.

### Quick test configuration

Use:

    SoloCrew Backend - Quick Tests

Task:

    test

These configurations use the local secrets file imported by
the Spring development and test profiles.

### Owner Web

Open the repository root in IntelliJ.

Use the integrated terminal:

    pnpm run dev:owner-web

The frontend and backend can run independently.

## Application profiles

The backend supports:

- dev: Local PostgreSQL development, readable logs and detailed health.
- test: Dedicated PostgreSQL test database and test logging.
- prod: Explicit external database configuration, structured JSON
  logging and restricted health details.

Production database settings must be supplied explicitly.

## Database migration conventions

Flyway migrations are stored under:

    backend/src/main/resources/db/migration/

Migration filenames follow:

    V<version>__<description>.sql

Examples:

    V1__create_platform_schema.sql
    V2__create_identity_tables.sql
    V3__create_customer_tables.sql

Rules:

- Never edit a migration that has already been applied in a shared
  environment.
- Add a new versioned migration for every schema change.
- Keep migrations deterministic and reviewable.
- Do not create application tables manually in DBeaver.
- Do not use schema.sql or data.sql alongside Flyway.
- Do not use Flyway clean against application databases.

DBeaver is for inspection and controlled troubleshooting.
Version-controlled Flyway migrations remain the source of truth.

## Local development principles

- Docker Compose is not required.
- PostgreSQL runs as a native local service.
- DBeaver is an optional database inspection client.
- Temporal will use its native development server.
- AI provider configuration will be introduced in the AI foundation slice.
- Local builds should not require paid AI APIs.
- External provider dependencies should have suitable mock implementations.
