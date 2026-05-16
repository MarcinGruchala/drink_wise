# Day Details Screen Design

## Goal

Add a Day Details screen that opens from the existing calendar when the user taps a date. The screen shows the selected date, a circular alcohol consumption indicator, and the drinks consumed on that day.

The first implementation should be intentionally small and reviewable. Stage 3 will use mock/sample data for the new screen so the layout and indicator can be reviewed before real data is wired in.

## Current Context

The app is an Android/Kotlin Jetpack Compose app using Material 3, Navigation Compose, Hilt, Room, DataStore Preferences, and `StateFlow`-backed ViewModels.

Relevant current pieces:

- `AppNavigation` owns the root `NavHost` and bottom navigation.
- `CalendarScreen` already renders a month pager and day cells with `CalendarDayData`.
- `AlcoholUnitLevelProgressIndicator` is the existing compact circular progress indicator used by calendar/home UI.
- `DrinkEntity` stores `quantity`, `alcoholContent`, and `timestamp`.
- `DrinkDao.getPaginatedDrinksByDateRange(startDate, endDate)` already supports date-range reads.
- Daily limits come from `AlcoholLimitPreferencesDataSource`.

The old branch `feature/edit-past-drinks` is a reference only. Useful parts are the `day_details/{epochDay}` route, calendar click wiring, repository date filtering, and scaffold/inset fix. The branch also contains unrelated project churn and a larger day-details implementation that should not be copied wholesale.

## Scaffold And Navigation

The app currently uses `enableEdgeToEdge()` in `MainActivity` and a root `Scaffold` in `AppNavigation`, while bottom-nav screens also use nested `Scaffold`s. Several destination screens ignore their `innerPadding`, which can draw content under system bars and creates risk of double or missing insets.

The clean fix is:

- Keep the root `Scaffold` responsible for the bottom navigation bar.
- Set root scaffold `contentWindowInsets = WindowInsets(0)` so nested destination scaffolds own status-bar/content insets.
- Apply each destination `Scaffold`'s `innerPadding` to its top-level content.
- Remove now-unnecessary `UnusedMaterial3ScaffoldPaddingParameter` suppressions.
- Hide bottom navigation when the current route is Day Details.

Add a route:

```kotlin
data object DayDetails : AppRoute("day_details/{epochDay}") {
    const val ARG_EPOCH_DAY = "epochDay"
    fun createRoute(epochDay: Long) = "day_details/$epochDay"
}
```

Calendar day cells will receive `onDayClick: (LocalDate) -> Unit`. Tapping a date navigates to `AppRoute.DayDetails.createRoute(date.toEpochDay())`.

## Stage 3 Design-First Screen

The initial Day Details screen should be reachable from the calendar and should show a complete visual layout with sample data. It should not depend on real Room data yet.

Portrait layout:

- Small top app bar with back arrow and selected date.
- Top content area with a large circular consumption indicator.
- A section below with a simple list of sample drinks.
- Empty/mock content should be clearly temporary in code but user-facing UI should look real.

Landscape will use the same simple vertical layout for this pass. The screen must remain usable after rotation, but a dedicated two-pane landscape layout is out of scope.

## Indicator Direction

For this pass, implement the simple "v1a" visual direction:

- A large circular badge based on the existing app indicator language.
- Normal or under-limit days use an enlarged version of the current circular progress style.
- Over-limit days switch to a more prominent warning badge:
  - saturated orange/red circular body,
  - readable inner face for the main percentage/value,
  - small attached overflow bubble on the rim,
  - bubble text shows the amount over the daily limit, such as `+1.6`.

The more complex continuous loop/ouroboros-like effect is explicitly out of scope for this pass. It can be refined later after the functional screen exists.

The Day Details indicator should be implemented as a reusable composable rather than embedding drawing logic directly in the screen.

## Logic Integration

After the design is approved:

- Add `DrinksRepository.getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>>`.
- Implement it by converting the selected date into local start/end-of-day epoch millis and reusing `DrinkDao.getPaginatedDrinksByDateRange`.
- Create `DayDetailsViewModel`.
- Read `epochDay` from `SavedStateHandle`.
- Combine drinks for the selected date with daily limit preferences.
- Calculate total units with existing `calculateAlcoholUnits`.
- Produce an `AlcoholUnitLevel` from the total and daily limit.

Suggested state:

```kotlin
data class DayDetailsState(
    val selectedDate: LocalDate,
    val drinks: List<DrinkEntity> = emptyList(),
    val totalUnits: Float = 0f,
    val dailyLimit: Float = 0f,
    val alcoholUnitLevel: AlcoholUnitLevel = AlcoholUnitLevel.Low(0f, 1f),
    val isLoading: Boolean = true
)
```

Error handling can stay minimal because the data source is local Room/DataStore Flow data. If the current architecture has no established local error state for these screens, do not introduce a new error pattern for this feature.

## Drink List UI

Each drink row should show:

- a generic drink icon,
- volume, formatted as `330 ml` or `1.5 L`,
- ABV percentage,
- calculated alcohol units,
- time of consumption.

Current `DrinkEntity` does not have a drink name, so do not invent naming behavior in this feature. Use neutral copy such as "Drink" only if a primary label is needed.

Drink rows are not clickable in this pass. Editing drinks is out of scope.

## Strings

All user-visible text must be added to `app/src/main/res/values/strings.xml`.

New strings will cover:

- back button content description,
- units label,
- over-limit label or accessibility text,
- no-drinks message,
- generic drink label,
- drink list section heading if used.

## Accessibility

The Day Details screen should provide:

- back button content description,
- tappable day cells with a reasonable content description,
- indicator semantics such as "`5.6` of `4.0` units, over daily limit by `1.6` units",
- drink row semantics including volume, ABV, units, and time when shown,
- minimum 48dp touch targets for clickable elements.

## Testing And Verification

Use `android-testing` when adding tests.

Reasonable tests for the full implementation:

- date-to-timestamp range conversion, especially local start/end of day,
- unit formatting helpers,
- volume formatting helpers,
- ViewModel state calculation if dependencies can be faked without large test infrastructure.

Verification:

- run `./gradlew test`,
- run a debug build task rather than release build,
- use Maestro for UI verification if an emulator/device is available, especially after navigation and visible screen behavior are wired.

## Out Of Scope

- Editing past drinks.
- Adding new drink names or categories.
- Swipe navigation between dates.
- Shared element transitions.
- Continuous loop/ouroboros overflow rendering.
- Release builds or signing changes.
- New navigation/state-management architecture.

## Acceptance Criteria

- Calendar day tap opens Day Details for that date.
- Day Details can navigate back to Calendar.
- Bottom navigation is hidden on Day Details.
- Bottom-nav screens no longer draw under the status bar after scaffold fix.
- Stage 3 stops with a reviewable mock/sample Day Details design.
- Final implementation uses real selected-date drinks and daily limit.
- Over-limit days show the v1a attached overflow badge.
- Temporary mock data is removed before final completion.
