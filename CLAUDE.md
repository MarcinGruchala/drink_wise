# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run unit tests for a specific module
./gradlew :app:test
./gradlew :alcohol-database:test
./gradlew :user-preferences:test

# Run Android instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

**Drink Wise** is an Android app for tracking alcohol consumption using alcohol units (volume × ABV / 1000).

### Module Structure

- **app** - Main application module with UI, navigation, and domain logic
- **alcohol-database** - Room database module for storing drink records
- **user-preferences** - DataStore-based preferences module for user settings

### Tech Stack

- **UI**: Jetpack Compose with Material 3
- **DI**: Hilt (all modules use `@InstallIn(SingletonComponent::class)`)
- **Database**: Room with KSP
- **Preferences**: DataStore Preferences
- **Navigation**: Compose Navigation with bottom navigation bar

### Key Patterns

**Data Flow**: ViewModels expose `StateFlow<State>` to Compose UI. Repository interfaces use Kotlin `Flow` for reactive data. Each ViewModel combines multiple flows (drinks, preferences) using `combine()` and `flatMapLatest()`.

**Dependency Injection**: Each module provides its own Hilt module:
- `AlcoholDatabaseModule` provides `AlcoholDatabase` and `DrinkDao`
- `UserPreferencesModule` provides `AlcoholLimitPreferencesDataSource` and `SummaryPeriodPreferencesDataSource`
- `DrinksRepositoryModule` provides `DrinksRepository`

**Time Abstraction**: `Clock` interface in `utils/time/` enables testable time-dependent code.

### App Screens

Four main screens via bottom navigation: Home, Calendar, Calculator, Settings.

### String Resources

All user-visible strings are in `app/src/main/res/values/strings.xml`. Use string resources for new UI text.

### Build Variants

- **debug**: Uses `.dev` application ID suffix
- **release**: Minified with ProGuard, requires `keystore.properties`
