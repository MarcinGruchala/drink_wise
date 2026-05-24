# Progress Ring Animation Design

## Goal

Add a subtle draw-on animation to selected alcohol unit progress rings so they feel alive when a screen loads or when Home summary periods change, while preserving the existing ring drawing, over-limit behavior, text, and accessibility semantics.

The animation should be a small polish layer, not a new data visualization. It applies to the large or summary indicators where motion adds clarity: Home summary cards, the Calendar month summary, and the Day Details consumption indicator.

## Current Context

Drink Wise is an Android/Kotlin Jetpack Compose app using Material 3, Hilt, Room, DataStore Preferences, Navigation Compose, and `StateFlow`-backed ViewModels.

Relevant existing pieces:

- `AlcoholUnitProgressRing` in `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt` owns the shared Canvas ring renderer, alcohol unit ratio calculation, base progress, overflow lap progress, overflow gap radius, and level color selection.
- `DrinksSummaryCard` in `app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt` uses the shared ring at 54dp for Today, This Week, and This Month.
- `HomeScreen` lets users switch each summary between start-of-period and rolling-period calculation modes. Those mode changes can alter each card's `AlcoholUnitLevel`.
- `MonthConsumptionIndicator` in `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt` uses the shared ring for the large monthly summary.
- `DayCell` in `CalendarScreen.kt` also uses the shared ring around individual dates, but those compact rings are dense calendar decoration and should remain static.
- `DayConsumptionIndicator` in `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt` uses the shared ring for the large daily indicator.
- `AlcoholUnitProgressRingTest` already covers the deterministic progress and overflow helper math.

No ViewModel, repository, database, user-preference, or alcohol calculation changes are needed. The feature is presentation-only.

## Scope

In scope:

- Add an opt-in animation API to `AlcoholUnitProgressRing`.
- Animate the displayed ratio from zero to the target ratio on first composition for opted-in rings.
- Animate the displayed ratio from its current displayed value to the next target ratio when the target changes.
- Enable the animation on Home summary card rings, the Calendar month summary ring, and the Day Details large ring.
- Preserve the current Canvas rendering details, including start angle, rounded arc caps, base lap behavior, overflow gap, overflow lap behavior, colors, stroke widths, and layout sizes.
- Keep existing visible text and accessibility descriptions tied to the final/current data state rather than intermediate animation frames.
- Verify with JVM tests and existing Maestro flows where an emulator or device is available.

Out of scope:

- Animating Calendar day-cell rings.
- Animating or counting the center/adjacent text values.
- Changing the `AlcoholUnitLevel` thresholds, unit calculations, summary-period calculations, or persistence.
- Redesigning Home cards, Calendar, or Day Details layouts.
- Adding new over-limit labels to Home or Calendar day cells.
- Changing the existing over-limit visual language.

## Approved Approach

Use an opt-in animation parameter on the shared ring component.

`AlcoholUnitProgressRing` remains the single source of truth for ring drawing and overflow behavior. It should expose a parameter such as `animateProgress: Boolean = false`. Existing and future call sites remain static by default unless they explicitly opt in. The same composable should also expose timing parameters for the draw-on animation so callers can tune duration and the initial start delay without owning animation state.

When `animateProgress` is `false`, the component should behave as it does today: calculate the target ratio from `AlcoholUnitLevel` and draw it immediately.

When `animateProgress` is `true`, the component should animate only the ratio value passed into the existing drawing logic. The renderer should still use the same `drawAlcoholUnitProgressRing` function and helper math so the animated and static rings cannot drift apart.

## Motion Behavior

The chosen motion feel is calm draw-on.

For opted-in rings:

- On first composition, the displayed ratio starts at `0f`.
- The displayed ratio animates to the target ratio.
- When the target ratio changes while the composable remains mounted, the displayed ratio animates from the current animated value to the new target ratio.
- The motion uses a Material-style emphasized decelerate easing: quick at the start, gentle at the end.
- The duration should be long enough to read as intentional polish but short enough not to delay comprehension. A default duration around 1200ms is appropriate after visual review because the original 800ms draw-on was easy to miss.
- The first draw-on animation may use a short start delay, around 300ms by default, so screen/navigation opening does not hide the beginning of the motion.
- The start delay applies only to the first animation for that ring instance. Later target changes, such as Home summary period switches, should animate immediately from the current displayed ratio to the new target ratio.
- Animation duration and initial start delay should be parameters on `AlcoholUnitProgressRing`, with defensive handling for negative values.
- There is no extra pulse, bounce, overshoot, scale, shimmer, or celebratory effect.

The animation should tolerate all valid ratios:

- `0f` remains visually empty.
- Ratios between `0f` and `1f` draw a partial base lap.
- `1f` draws a full base lap.
- Ratios above `1f` move through the existing over-limit rendering as the displayed ratio crosses the limit.
- Very large over-limit ratios should still use the existing current-lap overflow calculation.

## Over-Limit Behavior

The animation must preserve the existing Compose Canvas over-limit effect.

For final ratios above the limit:

- The base ring becomes a full lap in the level color.
- The current over-limit lap is drawn from the top of the circle.
- The cleared moving gap remains at the overflow lap head.
- The stroke cap, start angle, gap sizing, and full-cycle overflow behavior remain unchanged.

During animation into an over-limit target, it is acceptable and desired for the ring to visibly progress from an under-limit partial arc, to a full base lap, and then into the overflow lap. That makes the over-limit transition understandable without introducing a separate animation path.

## Call-Site Behavior

Home summary cards:

- Today, This Week, and This Month rings opt in to animation.
- First screen load animates each ring from zero to its current target.
- Switching a card between start-of-period and rolling-period modes animates from the currently displayed ratio to the newly selected mode's ratio.
- Card expansion/collapse behavior and segmented controls remain unchanged.

Calendar month summary:

- The large monthly summary ring opts in to animation.
- Opening Calendar animates the visible month summary from zero to its target.
- Moving between months may animate each newly composed month summary from zero to its month target. This is acceptable because monthly paging already introduces a larger context change.
- Individual date rings remain static.

Day Details:

- The large day consumption ring opts in to animation.
- Opening a date detail page animates the ring from zero to its target.
- The centered percent/unit text and over-limit label appear with final values immediately, as they do today.

## Component Boundaries

Responsibilities should remain narrow:

- `AlcoholUnitProgressRing`: ratio calculation, optional animation, level color selection, and Canvas ring drawing.
- Home, Calendar, and Day Details callers: decide whether their usage should animate and keep their existing layout concerns.
- ViewModels and domain classes: continue exposing the final `AlcoholUnitLevel`; they should not know about animation state.

This keeps animation close to the visual primitive while still making it opt-in per product surface.

## Accessibility

The animation should not create new accessibility announcements or expose transient animation frames.

- Existing content descriptions should continue to describe the final/current consumption state.
- Calendar day cells remain buttons with date-focused descriptions; their small rings remain decorative and static.
- Home visible text remains the primary compact summary signal beside the decorative ring.
- Day Details and Calendar month summaries keep their existing detailed descriptions.

Because the animation is purely visual polish, assistive technologies should not need to track each frame.

## Testing

JVM tests should keep covering deterministic helper behavior in `AlcoholUnitProgressRingTest`.

If the implementation introduces a small helper to choose the displayed ratio behavior or animation target, add focused unit tests for that helper. Do not try to unit-test Compose animation frames through timing-sensitive assertions.

Verification should include:

- `./gradlew test`,
- visual/manual check of Home initial load and Home summary period toggles,
- visual/manual check of Calendar month summary, including an over-limit month,
- visual/manual check of Day Details, including an over-limit day,
- Maestro verification with existing flows under `maestro/flows/` when an emulator or connected device is available.

Relevant existing Maestro flows:

- `maestro/flows/home-smoke.yaml`,
- `maestro/flows/calendar-day-details.yaml`,
- `maestro/flows/calendar-day-details-manage-drinks.yaml` if drink editing is touched or needs regression coverage.

## Risks And Mitigations

Risk: enabling animation by default could make dense Calendar day cells feel noisy or expensive.

Mitigation: keep animation opt-in and leave `animateProgress` defaulted to `false`.

Risk: implementing a separate animated renderer could accidentally change the over-limit gap behavior.

Mitigation: animate only the ratio value and continue using the existing Canvas renderer and helper math for both static and animated rings.

Risk: text and ring can briefly disagree while the ring animates toward the final value.

Mitigation: accept this as intentional. Text remains truthful and stable; the ring is a visual transition toward that state.

Risk: very short animations may look like a jump, while long animations may feel sluggish.

Mitigation: use one shared calm draw-on duration and verify on real/emulated devices. Tune only if the first pass feels distracting or too slow.

Risk: Compose animation APIs may trigger unnecessary recomposition.

Mitigation: keep animation state local to the ring, use standard Compose animation primitives, and avoid propagating per-frame values into parent composables or ViewModels.
