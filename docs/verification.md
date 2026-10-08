# Verification

## Automated checks

- Frontend: 8 Vitest tests cover Planned/Published requirements, duplication without old metadata, link schemes, Bangkok conversion, preserving local time across DST, rejecting nonexistent times, and actual publication day on the calendar.
- Backend: 8 validation tests and 4 integration tests against real PostgreSQL. Integration tests cover persistence, stale revision rejection, invalid import leaving data unchanged, backup round-trip and rejecting writes from an external Origin. Validation also covers whitespace around pasted HTTP links and profile URL validation with legacy-backup compatibility.
- TypeScript checking and production Vite build pass. The editor/calendar are separate lazy-loaded chunks.

Run frontend checks with `npm.cmd test` and `npm.cmd run build` in `frontend`.

Run the backend integration suite on a separate test database:

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\createdb.exe' -h 127.0.0.1 -p 55432 -U planny planny_test
$env:PLANNY_TEST_DB_URL='jdbc:postgresql://127.0.0.1:55432/planny_test'
cd backend
.\mvnw.cmd test
```

Tests roll back changes and are enabled only when the URL explicitly names `planny_test`. Without that variable the integration class is skipped; validation tests still run.

## Browser smoke

Playwright using installed Microsoft Edge, a separate backend/test database, and synthetic Thai content verified:

1. Create an idea, edit rich brief/script, select multiple platforms, save distinct captions and a shared schedule to PostgreSQL.
2. Change the shared script without overwriting platform captions; create and apply a Tag.
3. Drag Planned on the month calendar and preserve 09:30 Bangkok local time.
4. Manually mark Published and add a platform post link; duplicate into Draft without old publication dates or links.
5. Archive/unarchive and Trash/restore without losing content metadata.
6. Export and Import a backup through the UI.
7. Abort a save request, verify browser draft retention, reload, choose recovery and successfully retry.

A final browser pass against the packaged jar verified immediate recovery capture of multi-line links before blur, cancelling a schedule back to Ready, rolling Published back with cleared actual time/post links, removing a Tag without removing content, deactivating a platform while retaining old destinations, permanent deletion, and loading the empty production board with bundled Vue assets. No page errors occurred.

No browser page errors occurred. Test workspace was restored afterward; production workspace starts empty. Screenshots under `.impeccable/review/` contain clearly marked synthetic test content.

## Visual review

Desktop board (1440px), desktop editor and mobile board (390px, reduced motion) were captured and reviewed. Mechanical Impeccable detector returned no findings. Independent finish reviewer returned **Ship**, with no material visual/UX findings in the provided evidence. The review assessed calendar/dialog/error states from source and smoke evidence rather than separate captures of each state.

## Scope of evidence

### Platform profile addition

Browser verification of the packaged application passed: four branded SVG logos; page URLs persisted after reload; “open page” launched the configured destination in a new tab (external response intercepted in the test); invalid URL schemes were rejected by both client and server; backup included page URLs; legacy schema-1 backups without pageUrl imported successfully; desktop and 390px profile had no page overflow or JavaScript errors. Test data was restored afterward. Logos use locally bundled [Simple Icons](https://github.com/simple-icons/simple-icons).

Final profile screenshots were recaptured from production without changing workspace data. Profile helper and status text use the existing muted color (#597082), with 4.80:1 contrast against the canvas. Independent finish reviewer returned **Ship** after confirming both contrast fixes, with no unresolved findings in the reviewed scope.

This covers the local core tool; it does not establish multi-user, internet deployment, social-platform integration or AI behavior. Full-history undo, multi-device draft synchronization and concurrent editing merges are outside the confirmed scope. Native date input formatting follows the browser locale, while app copy is Thai.
