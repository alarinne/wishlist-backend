# wishlist

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
