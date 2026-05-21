# Alcohol Unit Indicator Overflow Design

## Goal

Make alcohol unit progress indicators behave consistently across the app when consumption crosses the configured limit, while preserving the compact Home and Calendar layouts.

The app already has the desired overflow behavior on Day Details: once consumption passes the limit, the ring remains full and the current over-limit lap is shown with a small moving gap. Home and Calendar currently use a simpler Material circular progress indicator that does not communicate over-limit progress as clearly. This migration brings the Day Details ring behavior to the shared compact indicator without adding extra labels to dense surfaces.

## Current Context

Drink Wise is an Android/Kotlin Jetpack Compose app using Material 3, Hilt, Room, DataStore Preferences, Navigation Compose, and `StateFlow`-backed ViewModels.

Relevant existing pieces:

- `DayConsumptionIndicator` in `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt` renders a custom 220dp Canvas ring with overflow lap behavior, centered percentage/unit text, and a Day Details-only `+x` over-limit label.
- `AlcoholUnitProgressRing` in `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt` renders the shared Canvas progress ring used by compact and Day Details indicators.
- `DrinksSummaryCard` uses the compact indicator at 54dp on Home.
- `CalendarScreen.DayCell` uses the compact indicator at day-cell size with a 3dp stroke.
- `DayConsumptionIndicatorOverflowTest` already covers the day-details overflow progress helpers.
- No ViewModel, repository, database, or domain changes are needed because all three surfaces already receive an `AlcoholUnitLevel` with `unitCount` and `limit`.

## Scope

In scope:

- Extract the Day Details overflow ring drawing and progress math into reusable common UI code.
- Use `AlcoholUnitProgressRing` directly for the compact Home and Calendar indicators.
- Update `DayConsumptionIndicator` to delegate ring drawing/math to the shared implementation.
- Preserve Home and Calendar layout sizes, positions, and visible text.
- Preserve the Day Details centered text, accessibility description, and `+x` over-limit label.
- Move overflow math tests to the shared implementation.
- Verify with JVM tests and existing Home/Calendar UI flows where available.

Out of scope:

- Redesigning Home summary cards.
- Redesigning Calendar day cells.
- Adding compact `+x` over-limit badges.
- Changing alcohol limit calculations, persistence, or user preferences.
- Changing unit formatting beyond reusing existing Day Details formatting where already used.

## Approved Approach

Use a shared overflow ring renderer.

The shared code should own:

- selecting the level color from `AlcoholUnitLevel`,
- calculating base progress for ratios at or below the limit,
- calculating the current overflow lap for ratios above the limit,
- drawing the track, full base circle, overflow arc, and cleared moving gap,
- defensive handling for zero or negative limits by clamping the limit to a small positive value before computing the ratio.

`DayConsumptionIndicator` remains a rich Day Details component. It keeps its layout and text content, but uses the shared ring renderer instead of owning the Canvas drawing details.

Home and Calendar call `AlcoholUnitProgressRing` directly. The ring keeps compact defaults for the existing Home track color and stroke width, while Calendar overrides only the 3dp stroke.

## Visual Behavior

For consumption at or below the limit:

- Home, Calendar, and Day Details show a proportional circular ring.
- The ring color still follows `AlcoholUnitLevel`: low, alarming, or high.
- Compact indicators keep their existing footprint and stroke widths.
- Day Details keeps its centered percentage and unit text.

For consumption above the limit:

- The base circle is fully filled in the level color.
- The current over-limit lap is drawn from the top of the circle.
- A small cleared gap appears at the moving head of the overflow lap, matching the Day Details behavior.
- Calendar shows only the compact ring around the day number.
- Home shows only the compact ring beside the existing `unitCount / limit` text.
- Day Details remains the only surface with an explicit `+x` over-limit label.

## Component Boundaries

The implementation should keep responsibilities narrow:

- Shared common renderer: visual ring drawing, compact defaults, and progress math only.
- `DayConsumptionIndicator`: Day Details composition, strings, semantics, center content, and over-limit label.
- Home and Calendar callers: compact ring sizing and stroke choices only.

This avoids a broad UI rewrite and prevents the Day Details and compact indicators from drifting apart again.

## Accessibility

Day Details keeps its existing detailed content description:

- under limit: consumed amount, limit, and percent of daily limit,
- over limit: consumed amount, limit, percent of daily limit, and amount over limit.

Calendar keeps the existing day-cell button content description. The compact indicator remains decorative inside the tappable date cell so it does not compete with the date action announcement.

Home keeps the existing visible text as the primary compact signal. The implementation should avoid introducing a second verbose announcement from the decorative ring unless testing shows the current compact indicator already exposes progress semantics that users rely on.

## Testing

JVM tests should cover the shared progress helpers:

- base progress follows the ratio below the limit,
- base progress is capped at one full lap,
- base progress does not render negative consumption,
- overflow progress is absent up to and including the limit,
- overflow progress follows the current lap after the limit,
- overflow progress handles large overages,
- overflow progress renders a full lap for exact over-limit cycles,
- ratio calculation clamps zero or negative limits defensively if that calculation lives in shared code.

Verification should include:

- `./gradlew test`,
- a focused visual/manual check through existing Home and Calendar flows,
- Maestro verification with existing flows under `maestro/flows/` when an emulator or device is available.

## Risks And Mitigations

Risk: the compact Calendar indicator is small, so the cleared overflow gap could be hard to see.

Mitigation: keep the shared gap proportional to stroke width, as Day Details does today, and verify Calendar visually.

Risk: replacing Material `CircularProgressIndicator` may alter semantics or animation behavior.

Mitigation: treat compact rings as decorative in Home and Calendar, preserve screen-level visible text/descriptions, and keep the renderer deterministic rather than introducing new animation scope.

Risk: track color changes could make compact rings feel different.

Mitigation: keep existing call-site stroke widths and choose track colors intentionally: Day Details should keep `surfaceVariant`; compact indicators should keep a compact-friendly Material color equivalent to the existing visual contrast unless the implementation review shows `inverseSurface` is too heavy with the custom renderer.
