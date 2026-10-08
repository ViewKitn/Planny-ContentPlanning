# Cloud deployment

Deploy the repository root using `Dockerfile.vercel`. The image builds Vue and bundles it into the Spring Boot JAR; the web UI and `/api` share one origin. The `cloud` Spring profile binds to `0.0.0.0` and `$PORT` and requires online PostgreSQL credentials.

Required environment variables (Production and Preview should use separate databases):

| Variable | Value |
| --- | --- |
| `PLANNY_DB_URL` | JDBC PostgreSQL URL with TLS, e.g. `jdbc:postgresql://HOST/planny?sslmode=require` |
| `PLANNY_DB_USER` | Database role |
| `PLANNY_DB_PASSWORD` | Database password, stored as a sensitive environment variable |
| `PLANNY_ACCESS_PASSWORD` | Private workspace password, at least 16 characters |
| `PLANNY_ALLOWED_ORIGINS` | Exact HTTPS origins separated by commas, for custom domains |

Vercel's deployment and production origins are also accepted from `VERCEL_URL` and `VERCEL_PROJECT_PRODUCTION_URL`. Cloud access uses HTTP Basic authentication with username `planny`. The browser requests credentials before loading any workspace data. Missing/short passwords fail startup. Local mode remains account-free.

Use a new, empty cloud database. Flyway initializes its schema automatically. Existing local content is transferred only by explicitly importing an exported backup. Do not include passwords in Git, screenshots, logs, or client-side environment variables.

Deployment is complete only after database provisioning, a successful Vercel build, and an online smoke test confirming that anonymous API access is denied and authenticated changes survive reload. A frontend-only deployment cannot store content with the local backend.
