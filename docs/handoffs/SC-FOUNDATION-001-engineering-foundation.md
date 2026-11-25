# SC-FOUNDATION-001 — Engineering Foundation

- Status: Complete
- Repository: techfirm123/solocrew
- Base branch: main

## Objective

Establish the SoloCrew repository, engineering conventions,
toolchain baseline and initial CI foundation.

## Delivered

- Monorepo repository structure.
- Owner Web and Control Center workspace placeholders.
- pnpm workspace configuration.
- Java 21 backend build foundation.
- Gradle 9.9.2 wrapper.
- Node.js 24 and pnpm 11.29.0 pinning.
- Editor configuration and Git conventions.
- Repository validation scripts.
- GitHub Actions CI workflow.
- Initial README and local development documentation.
- Engineering baseline ADR.

## Engineering baseline

| Component | Version |
|---|---:|
| Java | 21 LTS |
| Gradle | 9.9.2 |
| Node.js | 24 LTS |
| pnpm | 11.29.0 |

## Validation

The local toolchain and repository validation commands were
successfully executed.

The repository was pushed to GitHub.

## Notes

The repository intentionally avoids a Docker Compose dependency
for local development.

The detailed engineering baseline is documented in:

    docs/decisions/ADR-0001-engineering-baseline.md
