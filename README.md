# Drink Wise

Drink Wise is an Android app for tracking alcohol consumption, reviewing drinking patterns, and keeping personal alcohol limits visible. It is built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, Hilt, and Navigation Compose.

## Features

- Log drinks with volume, alcohol percentage, quantity, and time.
- View daily, weekly, and monthly alcohol unit summaries.
- Browse consumption history from a calendar and inspect individual day details.
- Add, edit, delete, and undo-delete drinks from the day details flow.
- Calculate alcohol units before recording a drink.
- Configure daily, weekly, and monthly consumption limits.

## Project Structure

- `app` - Compose UI, navigation, ViewModels, dependency injection, and app-domain logic.
- `alcohol-database` - Room database, DAO, entities, and persistence wiring for drink records.
- `user-preferences` - DataStore-backed settings for alcohol limits and summary period preferences.
- `maestro/flows` - Maestro UI verification flows for core app screens.

## Tech Stack

- Kotlin and Gradle Kotlin DSL
- Jetpack Compose with Material 3
- Navigation Compose
- Hilt dependency injection
- Room with Kotlin Flow
- DataStore Preferences
- JUnit 5 for local tests
- Maestro for Android UI verification

## Getting Started

Open the project in Android Studio, or use the Gradle wrapper from the repository root.

```sh
./gradlew test
```

Run a full local build:

```sh
./gradlew build
```

Run instrumented tests when an emulator or device is available:

```sh
./gradlew connectedAndroidTest
```

Run a Maestro flow on a connected emulator or device:

```sh
scripts/android-maestro-run.sh maestro/flows/home-smoke.yaml
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
