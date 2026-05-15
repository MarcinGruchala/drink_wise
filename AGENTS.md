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
- Use Maestro for Android UI verification when changing Compose screens,
  navigation, or user-visible flows. Prefer existing flows under
  `maestro/flows/`; add or update a focused flow for new UI behavior.
- Use Maestro video recording for animation-heavy or transition-heavy UI
  changes.
