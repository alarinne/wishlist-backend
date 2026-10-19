# wishlist

## Local Docker Startup

Use Docker Desktop with Linux containers and Docker Compose v2. Run the following
PowerShell commands from this repository's root. Java 17 and Maven are supplied
by the Docker build; they are not required on the host for this startup path.

```powershell
if (-not (Test-Path -LiteralPath .env)) {
    Copy-Item -LiteralPath .env.example -Destination .env
}
docker compose config --quiet
docker compose up -d --build --wait --wait-timeout 180
docker compose ps
docker compose logs --tail 100 backend
```

Keep an existing `.env`: it contains the settings for your existing database.
Never commit it or copy credentials into the image. The example credentials are
for a local learning environment only. The first build can download images and
Maven dependencies; `--build` includes the current checkout rather than relying
on an older image. The Dockerfile skips tests, so a successful build is not a
passing test suite. Check out the branch/commit you intend to run before building.

PostgreSQL is checked over TCP inside its container using `pg_isready`, with a
2-second probe timeout, 5-second interval, 3-second Docker timeout, 20-second
startup grace period, and 12 retries. The backend waits for `service_healthy`
before starting. The database name/user are expanded from the container's runtime
environment, not copied as literal secrets into the healthcheck.

This is a startup gate, not continuous dependency supervision. `pg_isready`
checks that the server accepts connections, not whether the backend credentials
are valid. `--wait` waits for database health and a running backend container;
there is no backend HTTP healthcheck. After the `Started DemoApplication` log,
verify the actual API (retry briefly if it is still starting):

```powershell
Invoke-RestMethod 'http://localhost:8081/api/categories'
Invoke-RestMethod 'http://localhost:8081/api/wishes'
```

An empty JSON array is a valid result. The root `/` is not an API readiness
endpoint. Flyway applies V1/V2 before Hibernate validates the schema. Preserve
important data/backups before applying migrations to an existing database.

### Ports And Troubleshooting

- Backend: `http://localhost:8081`, published only on `127.0.0.1`.
- PostgreSQL for host tools: `127.0.0.1:5433` with `.env.example`; the host port
  can be changed through `POSTGRES_PORT` in your `.env`.
- Backend-to-database connection: `postgres:5432` inside Compose, not the host
  database port. Changing the published PostgreSQL port does not change JDBC.
- Existing data stays in the `wishlist_postgres_data` named volume. Changing
  `.env` database credentials does not change accounts in an initialized volume.
- Do not start a second local backend on 8081. Do not kill unknown port owners.
- Local-only ports intentionally do not provide LAN/public access. Authentication
  and ownership are not implemented; this is not a public deployment setup.

For a failed/unhealthy database, build error, or restarting backend, inspect:

```powershell
docker compose ps --all
docker compose logs --tail 100 postgres backend
```

An unhealthy database blocks backend startup instead of claiming readiness.
Check existing `.env`, port ownership, and logs. Do not delete the database
volume to fix a password or startup error. Do not share logs/configuration that
contain credentials. After fixing configuration, run `up` again; use `--build`
when backend source changes.

### Frontend And Smoke Check

The frontend is not containerized in this task. Open another terminal in the
sibling `wishlist-frontend` repository, using a supported Node version (the
current Angular 21 CLI supports Node 20.19.x+, 22.12.x+, or 24+):

```powershell
npm.cmd ci
npm.cmd start -- --host 127.0.0.1 --port 4200
```

Open `http://localhost:4200/wishes`, not `http://127.0.0.1:4200`: the existing
backend CORS allows exactly `http://localhost:4200`. If 4200 belongs to another
app, stop it from its own terminal only when appropriate; using 4201 requires a
separate proxy/CORS configuration change. No wildcard CORS is needed here.

For a manual write smoke check, use a disposable category/wish: create, edit,
mark purchased, restore active, combine search/status/category filters, refresh,
and delete only the disposable wish. Category deletion is not implemented in
the UI. These steps modify the selected database; do not use important records.

### Stop And Start Without Losing Data

Stop the frontend with Ctrl+C. From this backend repository:

```powershell
docker compose stop
docker compose up -d --wait --wait-timeout 180
```

The second command starts the existing setup and checks database readiness again.
Use `--build` as well after backend source changes. The named volume survives
normal stop/start and image rebuilds. Do not use `docker compose down -v`,
`docker volume rm`, or volume pruning for an ordinary stop: these can delete
database data. Starting a live stack/migrations should be intentional; isolated
test runs must use distinct container names, ports, project name and volume.

References: [Compose startup order](https://docs.docker.com/compose/how-tos/startup-order/),
[pg_isready](https://www.postgresql.org/docs/16/app-pg-isready.html).

## Wish Status API

`PATCH /api/wishes/{id}/status` accepts `ACTIVE` or `PURCHASED` and returns the
updated wish with HTTP 200. Repeating the current status also succeeds. Only the
status changes; creating a wish still defaults to `ACTIVE`, and editing its other
details preserves its status.

```powershell
Invoke-RestMethod -Method Patch -Uri 'http://localhost:8081/api/wishes/1/status' -ContentType 'application/json' -Body '{"status":"PURCHASED"}'
```

Use `{"status":"ACTIVE"}` to restore an active wish. A missing wish returns 404.
Missing or invalid status input returns 400; numeric enum values are rejected.
Validation errors include `fieldErrors`; unreadable JSON returns the generic
`Invalid request body` message.

Flyway V2 extends the database status constraint without changing V1 or existing
wishes. Wish endpoints follow `WishApi -> WishController -> WishFacade -> WishService -> WishRepository`.

The project currently has no authentication or wish ownership checks. Anyone who
can reach the API can change a wish by ID; `PURCHASED` is a wishlist state, not
payment confirmation.

## Verification

```powershell
.\mvnw.cmd -o test
```

Tests cover status transitions, repeated requests, persisted fields, validation,
CRUD routes, CORS, and a Flyway V1-to-V2 upgrade with an existing wish.
