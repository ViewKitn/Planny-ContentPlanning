# Cloud deployment

Deploy the repository root using `Dockerfile.vercel`. The image builds Vue and bundles it into the Spring Boot JAR; the web UI and `/api` share one origin. The `cloud` Spring profile binds to `0.0.0.0` and `$PORT` and requires online PostgreSQL credentials.

Connect the Neon database to Production using Vercel Storage. Its sensitive `DATABASE_URL` (or `POSTGRES_URL`) is read directly by the cloud profile; database credentials are extracted server-side. Preview should use a separate database before enabling it.

Environment variables:

| Variable | Value |
| --- | --- |
| `PLANNY_DB_URL` | Optional JDBC PostgreSQL URL override with TLS |
| `PLANNY_DB_USER` | Database role when using the JDBC override |
| `PLANNY_DB_PASSWORD` | Database password when using the JDBC override |
| `PLANNY_ACCESS_PASSWORD` | Private workspace password, at least 16 characters |
| `PLANNY_ALLOWED_ORIGINS` | Exact HTTPS origins separated by commas, for custom domains |

Vercel's deployment and production origins are also accepted from `VERCEL_URL` and `VERCEL_PROJECT_PRODUCTION_URL`. The public web shell shows Planny's Login page; workspace APIs require a signed session cookie. Sign in with username `planny` and the existing `PLANNY_ACCESS_PASSWORD`. Missing/short passwords fail startup. Local mode remains account-free.

Login uses `POST /api/auth/login`; session status is `GET /api/auth/session`; logout is `POST /api/auth/logout`. The session lasts 12 hours and survives refresh. Its cookie is HttpOnly, Secure in cloud mode, SameSite=Strict, and signed using the configured password, so different container instances can validate it without an in-memory session store. Passwords are never persisted in browser storage. Logout clears the cookie; changing the configured password invalidates existing sessions. Anonymous API failures return JSON 401 without a `WWW-Authenticate` header, so the browser does not show a Basic Auth prompt.

Use a new, empty cloud database. Flyway initializes its schema automatically. Existing local content is transferred only by explicitly importing an exported backup. Do not include passwords in Git, screenshots, logs, or client-side environment variables.

Deployment is complete only after database provisioning, a successful Vercel build, and an online smoke test confirming that anonymous API access is denied and authenticated changes survive reload. A frontend-only deployment cannot store content with the local backend.

Production URL: https://planny-content-planning.vercel.app/ . Neon Free in Singapore is connected to Production only. Online verification passed authenticated HTML/health, content persistence after a fresh read, backup export, stale revision rejection (409), and foreign-origin rejection (403); temporary test content was removed. Anonymous API access returned 401. Online client requests allow 60 seconds for container/database cold starts; localhost retains 12 seconds.
