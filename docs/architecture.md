# Implementation

## Runtime

Vue 3 + TypeScript + Vite frontend; Spring Boot 3.5 on Java 21; PostgreSQL with Flyway migrations. Tiptap edits structured JSON documents (paragraphs, headings, bold and lists), rather than storing arbitrary rendered HTML. Thai and Latin fonts are served locally in the build.

The packaged jar serves Vue and `/api` together. Development uses Vite's `/api` proxy to backend port 18080. All services bind to loopback; this version has no application accounts and is intended for a single local creator.

## Personal workspace storage

A single `workspace` row contains the complete personal workspace as PostgreSQL JSONB, a monotonically increasing revision, and an update timestamp. This keeps content, tags, platform history, archive/trash and backup replacement atomic for the confirmed single-user scope. This is an implementation choice for the local tool, not a multi-user data model; adding accounts will require an ownership and isolation model.

Every write validates all records and updates only the expected revision. A stale browser receives HTTP 409 instead of overwriting newer data. The frontend serializes writes, preserves the local editor draft on failure, and requires loading the latest data before retrying after a conflict. SQL parameters are bound through JdbcTemplate.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/state` | Current workspace and revision |
| PUT | `/api/state` | Validated atomic replacement with expected revision |
| GET | `/api/backup` | Schema-versioned backup |
| POST | `/api/import` | Validate and atomically replace from backup with expected revision |
| GET | `/api/health` | Check application and database availability |

Imports reject unsupported schema versions, invalid states/dates/timezones, duplicate IDs/names, unknown tag/platform references, invalid document nodes and non-HTTP(S) links before changing data. Workspace payloads are limited to 8 MB. No API fetches user-provided links or performs social posting.

## Time and editor recovery

Publication timestamps are stored as instants. Temporal converts datetime inputs and calendar dragging in the chosen IANA timezone, preserving local time across date changes. Ambiguous/nonexistent times at daylight-saving transitions are rejected rather than guessed.

The canonical saved data is in PostgreSQL. Browser localStorage contains only recoverable editing drafts; recovering a draft is an explicit choice and does not write the server until the user edits or saves. Unsaved link input is captured along with the writing. Import replaces canonical data only after confirmation and clears obsolete drafts in the importing browser.

## References

- [Spring Boot system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Vite guide](https://vite.dev/guide/)
