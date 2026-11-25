# Local development setup

## Required tools

| Tool | Required version |
|---|---:|
| Git | Current supported release |
| Java JDK | 21 LTS |
| Node.js | 24 LTS |
| pnpm | 11.29.0 |
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

### 4. Validate the local toolchain

From the repository root:

    ./scripts/doctor.sh

Expected versions:

- Java major version 21
- Node.js major version 24
- pnpm version 11.29.0

## Install workspace dependencies

From the repository root:

    pnpm install --frozen-lockfile

## Backend build

Navigate to the backend directory:

    cd backend

Check the Gradle version:

    ./gradlew --version

The bootstrap launcher downloads Gradle 9.9.2 into GRADLE_USER_HOME
on first use. It verifies the distribution SHA-256 checksum before
extracting and executing Gradle.

A globally installed Gradle is not required.

## Generate the standard Gradle Wrapper

Once the bootstrap launcher has successfully executed, generate
the standard Gradle Wrapper:

    ./gradlew wrapper \
      --gradle-version=9.9.2 \
      --distribution-type=bin \
      --gradle-distribution-sha256-sum=971fcd7150ca4ba04852c0b2ca0d2d0ab9468e14b03473eab19528809914c76b

Verify the generated wrapper JAR:

    shasum -a 256 gradle/wrapper/gradle-wrapper.jar

Expected SHA-256:

    621008afc4f5d59357fcf06ffa644dd5aade5e091bdc363742710445f73cdaf8

Commit all generated Gradle Wrapper files.

## Validate the backend

From the backend directory:

    ./gradlew clean check

The backend uses a Java 21 Gradle toolchain.

Automatic JDK toolchain downloads are disabled. Install JDK 21 locally.

## Run the backend

From the backend directory:

    ./gradlew bootRun

The default development profile starts the application on:

    http://localhost:8080

Health endpoints:

    http://localhost:8080/actuator/health
    http://localhost:8080/actuator/health/liveness
    http://localhost:8080/actuator/health/readiness

## Application profiles

The application supports:

- dev: Local development, readable logs and detailed local health.
- test: Test configuration.
- prod: Structured JSON logging and restricted health details.

Run with the production profile:

    SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun

## Repository validation

Return to the repository root:

    cd ..

Run:

    pnpm run check

## Local development principles

- Docker Compose is not required.
- PostgreSQL will be installed and configured separately.
- Temporal will use its native development server.
- AI provider configuration will be introduced in the AI foundation slice.
- Local builds should not require paid AI APIs.
- External provider dependencies should have suitable mock implementations.

## Not included in this slice

- PostgreSQL integration.
- Flyway migrations.
- Authentication and authorization.
- Next.js application screens.
- Temporal workflow implementation.
- AI Model Gateway implementation.

These will be introduced incrementally through subsequent vertical slices.
