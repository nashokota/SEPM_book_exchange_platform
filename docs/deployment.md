# Deployment Notes

## Live URL

- Production URL: `https://sepm-book-exchange-platform.onrender.com/`

## Deployment Platform

The application is deployed on Render as a Docker-based web service backed by a PostgreSQL database.

## Deployment Architecture

Production deployment uses:
- Render Web Service for the Spring Boot application
- Render PostgreSQL database for persistent production data
- GitHub Actions for staged CI and deployment triggering
- Render deploy hook for automated redeployment from `main`

## CI/CD Flow

The project uses a staged pipeline:

1. Service and unit tests run first.
2. Repository and integration tests run second.
3. If code is pushed to `main` and both stages pass, GitHub Actions triggers the Render deploy hook.
4. Render rebuilds the Docker image and deploys the latest release.

## Branch Workflow

Recommended flow:
- `feature/*` branches are merged into `develop`
- `develop` is used as the active integration branch
- release PRs are opened from `develop` to `main`
- pushes to `main` trigger production deployment
- after release, `main` can be synced back into `develop`

## Docker Deployment Basis

The deployment is based on the project Docker setup:
- multi-stage `Dockerfile`
- `docker-compose.yml` for local full-stack execution
- environment variable driven configuration

The Render web service builds directly from the repository `Dockerfile`.

## Application Runtime Configuration

Important runtime behavior:
- application binds to `0.0.0.0`
- application port supports `PORT`
- datasource supports `SPRING_DATASOURCE_URL` override
- local fallback environment variables remain available for non-production runs

## Required Environment Variables

Set these in the Render web service:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `ADMIN_FULL_NAME`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`

Render provides `PORT` automatically.

## GitHub Actions Secret

Required repository secret:
- `RENDER_DEPLOY_HOOK_URL`

This secret is used by the `deploy-to-render` job in `.github/workflows/ci.yml`.

## Local Full-Stack Docker Command

For local full-stack validation:

```bash
docker compose up --build
```

This runs:
- PostgreSQL container
- Spring Boot application container

## Deployment Verification Checklist

After a release to `main`, verify:
- GitHub Actions `unit-tests` passed
- GitHub Actions `integration-and-repository-tests` passed
- GitHub Actions `deploy-to-render` executed
- Render started a new deployment
- public URL loads successfully
- login works
- public browse books works
- admin page loads
- database-backed flows work in production

## Common Deployment Notes

- Render may auto-detect the open application port during first deployment.
- The service branch should remain `main` for production releases.
- The deploy hook should be triggered only after successful CI on `main`.
- Local Docker and production Render environments use the same application image structure, but different runtime environment variables.
