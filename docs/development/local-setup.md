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

At the psql prompt, create the application login:

    CREATE ROLE solocrew_app LOGIN;

Set its password:

    \password solocrew_app

Create the development and test databases:

    CREATE DATABASE solocrew_dev OWNER solocrew_app;
    CREATE DATABASE solocrew_test OWNER solocrew_app;

Verify the databases:

    \l

Exit:

    \q

These are one-time setup commands. Do not execute the CREATE ROLE
or CREATE DATABASE commands again if the objects already exist.

The application role owns both databases. It does not need PostgreSQL
superuser privileges.

### 6. Verify database connectivity

Verify the development database:

    psql -h localhost -U solocrew_app -d solocrew_dev \
      -c "SELECT current_database(), current_user, version();"

Verify the test database:

    psql -h localhost -U solocrew_app -d solocrew_test \
      -c "SELECT current_database(), current_user, version();"

Both commands should connect using the application user's password.

### 7. Configure DBeaver

Create a new PostgreSQL connection in DBeaver.

Development connection:

| Setting | Value |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | solocrew_dev |
| Username | solocrew_app |
| Password | Your local database password |

Test connection:

| Setting | Value |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | solocrew_test |
| Username | solocrew_app |
| Password | Your local database password |

Use DBeaver to inspect schemas, tables and Flyway migration history.

After the backend starts for the first time, refresh the database tree.
You should see the platform schema and the Flyway migration history table.

### 8. Configure the application password

The backend reads the local database password from:

    SOLOCREW_DATABASE_PASSWORD

Set it in your terminal session:

    export SOLOCREW_DATABASE_PASSWORD='your-local-password'

Do not commit this value to Git.

For IntelliJ run configurations, configure the same variable in the
environment variables section.

The database URL and username have local development defaults.
They can be overridden using:

- SOLOCREW_DATABASE_URL
- SOLOCREW_DATABASE_USERNAME

## Install workspace dependencies

From the repository root:

    pnpm install --frozen-lockfile

## Backend build

Navigate to the backend directory:

    cd backend

Check the Gradle version:

    ./gradlew --version

The standard Gradle Wrapper downloads the pinned Gradle 9.9.2
distribution on first use.

A globally installed Gradle is not required.

## Validate the backend

The test profile connects to solocrew_test.

Ensure SOLOCREW_DATABASE_PASSWORD is set in the current shell.

From the backend directory:

    ./gradlew clean check

This runs the backend tests, including PostgreSQL and Flyway
integration tests.

The test profile does not use solocrew_dev.

## Run the backend

From the backend directory:

    ./gradlew bootRun

The default development profile connects to solocrew_dev.

The backend starts on:

    http://localhost:8080

Health endpoints:

    http://localhost:8080/actuator/health
    http://localhost:8080/actuator/health/liveness
    http://localhost:8080/actuator/health/readiness

## Application profiles

The application supports:

- dev: Local PostgreSQL development, readable logs and detailed health.
- test: Dedicated PostgreSQL test database and test logging.
- prod: Explicit external database configuration, structured JSON
  logging and restricted health details.

Run with the production profile:

    SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun

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

DBeaver is for inspection and controlled troubleshooting. The
version-controlled migrations remain the source of truth.

## Repository validation

Return to the repository root:

    cd ..

Run:

    pnpm run check

## Local development principles

- Docker Compose is not required.
- PostgreSQL runs as a native local service.
- DBeaver is an optional database inspection client.
- Temporal will use its native development server.
- AI provider configuration will be introduced in the AI foundation slice.
- Local builds should not require paid AI APIs.
- External provider dependencies should have suitable mock implementations.
