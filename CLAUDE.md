# CLAUDE.md — sista-android

Kotlin + Jetpack Compose, multi-module (`app`, `core/*`, `feature/*`). The backend is `Kemalhafizh/sistem-terpadu` (Laravel); every feature here must match an endpoint there. Read this before changing anything. CI enforces several of the rules below.

## Aturan main

### 1. Tidak ada angka karangan
What the app shows comes from the server.
- No `Random`, `.random()` or `.shuffled()` used to fill a screen, and no invented defaults posing as data: `tahfidzTargetJuz = 30`, `totalStudents = 36`, a sample file name, an announcement id like `"ann1"`, an author like "Pusat Informasi Sekolah".
- A figure the server didn't send is `null` in the model and "–" on screen. Model fields the backend may leave out are nullable, not defaulted to 0.
- An empty list is an `EmptyState` that says what is missing, not a placeholder card.
- **CI guard:** `app/src/test/.../architecture/NoFabricatedRandomnessTest.kt` fails on new randomness in `src/main`. Random UUIDs are fine. Legitimate randomness (animation, unpredictable proctoring) goes in its `allowed` list with the reason.

### 2. Aksi harus tersimpan
A button that changes something calls the server. Don't change only local state and leave it at that: it comes back on reload.
- Update optimistically if you like, but roll back and show the server's message when the call fails.
- If the backend has no endpoint yet, add the endpoint in sistem-terpadu first, or don't show the button.
- Show the server's own message (`serverMessageOf` in `core/data`), never just "Kode: 403".

### 3. Satu aturan akses
What each role may open is decided by the server.
- Use `capabilities` (`FeatureCatalog`, `LocalCapabilityState.canOpen(route)`, `guardedComposable`). Don't add role checks or role lists in screens.
- Menu entries and shortcuts navigate to real routes built with `createRoute(...)`, never a pattern with a literal `{placeholder}`. `FeatureCatalogTest` checks this.
- Screens about one child or one class carry its id/uuid in the route (`studentUuid`, `classroomId`). Don't fall back to "the first one".
- A ViewModel loads only what its own screen needs. Calling another role's endpoints just produces 403s.

### 4. Setiap PR punya test
- Pure logic (formatting, grouping, mapping) gets a JVM unit test.
- Screens get Roborazzi screenshots in `module/screenshots/` for loaded, empty, error and loading, plus dark mode for the main state.
- Don't weaken or delete a test to get green. If a test pinned the old wrong behaviour, update it to the new invariant and say so in the PR.

## UI
- Build screens with `:core:ui` (`SistaTopBar`, `SistaCard`, `StatTile`, `StatusPill`, `InlineBanner`, `EmptyState`, `ErrorState`, `SkeletonList`, `FilterChipRow`, theme tokens `SistaTheme`, `Spacing`, `StatusTone`), not the old `Sulaone*` components.
- Pattern: a stateful `XxxScreen(viewModel, …)` that collects state, plus a stateless `XxxContent(state, callbacks)` used by previews and screenshots.
- Every role's home uses the same layout; only the features shown differ.
- Times are in `Asia/Jakarta`. Numbers and dates follow the app's language (`12.480` / `12,480`), with Latin digits for Arabic like the web.

### Tiga bahasa
The app is offered in Indonesian, English and Arabic, like the web (`users.preferred_locale` is shared).
- Text on screen comes from `res/values/strings.xml` (Indonesian) with the same keys in `values-en` and `values-ar`: `stringResource(...)` in Compose, `UiText` (`:core:ui`) from ViewModels and formatting functions. Never a literal.
- Server data (names, classes, the server's own messages) is shown as sent; the server already answers in the user's language.
- Never read meaning out of a server message or title: it is translated. Act on the stable code beside it (`error_code`, `code`, `category_code`); ask for one in sistem-terpadu if it is missing.
- Arabic is right-to-left: use `start`/`end`, never `left`/`right`. Screenshot tests run in Indonesian (`in-` qualifier); add `en-`/`ar-ldrtl-` screenshots for new screens.
- **CI guards:** `LocalizationTest` fails when a key is missing in one language or placeholders differ; `HardcodedUiTextTest` fails on new on-screen literals. Its `debt` list only shrinks: translate a file, then lower or remove its entry.
- Language is applied by `AppLocale` and kept in sync with the account by `LocaleSync` (`PUT me/locale`).

## Sebelum push
```bash
export ANDROID_HOME=/opt/android-sdk JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew :feature:<module>:recordRoborazziDebug   # after UI changes; commit the screenshots
```
A module needs `:core:ui`, the Roborazzi plugin and `includeAndroidResources` before it can record screenshots (see `feature/admin/build.gradle`).

## Git
- Commit messages and PR descriptions in Indonesian. Describe what was wrong, why, and what changed. Link the matching sistem-terpadu PR when the API changes.
- No attribution lines (`Co-Authored-By`, `Claude-Session`) in commits, and no "Generated with…" footer or session link in PR descriptions.
- Don't merge PRs or rewrite `main`. Never commit `local.properties`, `gradle/gradle-daemon-jvm.properties`, keystores or API keys.
