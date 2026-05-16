# AGENTS.md

## Project

Drink Wise is an Android/Kotlin app for tracking alcohol consumption.

## Commands

- `./gradlew test` - run unit tests.
- `./gradlew build` - run a full local build.
- `./gradlew connectedAndroidTest` - run instrumented tests; requires an
  emulator or device.
- `scripts/android-maestro-run.sh` - build, install, and run a Maestro flow
  on an emulator or connected device.

Avoid release builds unless explicitly requested. The release variant is
minified and depends on local signing configuration in `keystore.properties`.

## Structure

- `app` - Compose UI, navigation, ViewModels, and app-domain logic.
- `alcohol-database` - Room database and drink persistence.
- `user-preferences` - DataStore-backed user settings.

The app uses Jetpack Compose Material 3, Hilt, Room/KSP, DataStore Preferences,
and Navigation Compose.

## Conventions

- ViewModels expose `StateFlow<State>` to Compose UI.
- Repository and data-source APIs use Kotlin `Flow` for reactive data.
- Use the injected `Clock` abstraction for time-dependent logic.
- Put all user-visible UI text in `app/src/main/res/values/strings.xml`.
- Use the `material-3` skill for Material Design 3 design-system work:
  color roles, typography, shape, tonal elevation, adaptive layout,
  component selection, theming, M3 Expressive, and MD3 compliance audits.
- Pair `material-3` with `jetpack-compose-android` for non-trivial Android
  Compose UI implementation so MD3 guidance is grounded in current Compose
  APIs and this app's architecture.
- Use the `jetpack-compose-android` skill for non-trivial Compose UI changes,
  reviews, performance work, navigation changes, animations, and accessibility.
- Use the `android-testing` skill when adding or updating JVM unit tests,
  ViewModel/Flow tests, Room/DataStore tests, Compose UI tests, or deciding
  between local tests, instrumentation, and Maestro flows.
- Use Maestro for Android UI verification when changing Compose screens,
  navigation, or user-visible flows. Prefer existing flows under
  `maestro/flows/`; add or update a focused flow for new UI behavior.
- Use Maestro video recording for animation-heavy or transition-heavy UI
  changes.
- Use Conventional Commits for new commits: `type(scope): description`, with
  scopes optional and types such as `feat`, `fix`, `docs`, `test`, `refactor`,
  `chore`, `build`, or `ci`.
- When asked to create an MR/PR, publish the current branch to GitHub and open
  a draft pull request. Inspect the worktree first and stage only the intended
  files.
