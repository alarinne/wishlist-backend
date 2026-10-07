# ADR-0001: Wish API And Facade Boundary

Date: 2026-10-07
Status: Accepted as part of the user-approved wish status API plan.

## Context

Wish endpoints previously called `WishService` directly. The latest project
instructions require simple backend flows to use an API interface, controller,
facade, service, and repository, with no service-to-service calls. Adding wish
status changes also expands the status contract stored by Flyway and consumed
by the Angular frontend.

Earlier project memory deferred facades. This decision applies the newer explicit
requirements within the affected wish boundary, without restructuring categories.

## Decision

- Define wish HTTP mappings, request bodies, and validation in `WishApi`.
- Keep HTTP response status decisions and the existing CORS origin in `WishController`.
- Make `WishController` delegate wish operations to `WishFacade`.
- Make `WishFacade` delegate to `WishService`; complex orchestration is unnecessary here.
- Keep repository access and entity-to-response mapping in `WishService`.
- Enter the public transactional status method through the service proxy from the facade.
- Mutate only the status of the managed wish and rely on transaction dirty checking.
- Accept named `ACTIVE` and `PURCHASED` values through a required enum DTO; reject numeric enum coercion using the Jackson 3 enum datatype setting.
- Add Flyway V2 to extend the named CHECK constraint while retaining V1 unchanged.
- Extend the Angular 21 response union; interactive status controls remain a later task.

## Consequences

The wish boundary now follows the mandated layers and has a reusable HTTP contract.
The facade currently delegates without business orchestration, adding a small amount
of code in exchange for consistent boundaries. Existing CRUD routes and response
shapes are retained; invalid JSON now has a stable generic error response across
wish and category endpoints. Numeric enum inputs are rejected for priority as well
as status.

Authentication, ownership, and optimistic concurrency are not provided by this
layering change. Category endpoints retain their older controller-to-service flow.

## Verification

- 37 backend tests passed, including persistence after the service transaction commits.
- Flyway V1-to-V2 upgrade was tested against an isolated H2 database with an existing wish.
- The exact V1 and V2 SQL files were applied to an isolated PostgreSQL 16.14 database;
  the existing wish survived, both statuses worked, and an unknown status was rejected.
- The Angular production build passed after the response type update.
