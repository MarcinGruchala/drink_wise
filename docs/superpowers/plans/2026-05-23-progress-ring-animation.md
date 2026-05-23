# Progress Ring Animation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add opt-in calm draw-on animation to selected alcohol unit progress rings while preserving the existing static default and over-limit Canvas behavior.

**Architecture:** Keep `AlcoholUnitProgressRing` as the single shared renderer. Add local Compose animation state that animates only the displayed ratio before handing it to the existing drawing function, then opt in Home summary cards, the Calendar month summary, and the Day Details large indicator.

**Tech Stack:** Kotlin, Jetpack Compose Canvas, Compose animation core `Animatable`, Material 3 motion easing, JUnit 5, Gradle, Maestro for Android UI verification.

---

## File Structure

- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt`
  - Add animation imports, shared motion constants, an opt-in `animateProgress` parameter, local `Animatable` state, and a small testable ratio-selection helper.
- Modify `app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt`
  - Add tests for the ratio-selection helper so static and animated draw paths stay explicit.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt`
  - Opt the 54dp Home summary ring into animation.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`
  - Opt the large monthly summary ring into animation and leave `DayCell` rings static.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`
  - Opt the 220dp Day Details ring into animation.
- Use existing verification flows:
  - `maestro/flows/home-smoke.yaml`
  - `maestro/flows/calendar-day-details.yaml`

---

### Task 1: Add Test Coverage For Animated Ratio Selection

**Files:**
- Modify: `app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt`

- [ ] **Step 1: Write failing helper tests**

Add these tests before the closing brace in `AlcoholUnitProgressRingTest`:

```kotlin
    @Test
    fun `draw ratio uses target ratio when progress animation is disabled`() {
        val drawRatio = resolveAlcoholUnitIndicatorDrawRatio(
            targetRatio = 1.38f,
            animatedRatio = 0.25f,
            animateProgress = false
        )

        assertEquals(1.38f, drawRatio, FLOAT_TOLERANCE)
    }

    @Test
    fun `draw ratio uses animated ratio when progress animation is enabled`() {
        val drawRatio = resolveAlcoholUnitIndicatorDrawRatio(
            targetRatio = 1.38f,
            animatedRatio = 0.25f,
            animateProgress = true
        )

        assertEquals(0.25f, drawRatio, FLOAT_TOLERANCE)
    }
```

- [ ] **Step 2: Run the targeted test to verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: FAIL with an unresolved reference for `resolveAlcoholUnitIndicatorDrawRatio`.

- [ ] **Step 3: Add the minimal helper implementation**

Add this helper in `AlcoholUnitProgressRing.kt` after `calculateAlcoholUnitIndicatorOverflowGapRadius` and before `alcoholUnitLevelIndicatorColor`:

```kotlin
internal fun resolveAlcoholUnitIndicatorDrawRatio(
    targetRatio: Float,
    animatedRatio: Float,
    animateProgress: Boolean
): Float {
    return if (animateProgress) animatedRatio else targetRatio
}
```

- [ ] **Step 4: Run the targeted test to verify it passes**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: PASS for all tests in `AlcoholUnitProgressRingTest`.

- [ ] **Step 5: Commit the helper test and implementation**

Run:

```bash
git add app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt
git commit -m "test: cover progress ring animation ratio selection"
```

---

### Task 2: Add Opt-In Animation To The Shared Ring

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt`

- [ ] **Step 1: Add Compose animation imports**

Add these imports near the top of `AlcoholUnitProgressRing.kt`:

```kotlin
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
```

- [ ] **Step 2: Add shared motion constants**

Add these constants below `DayDetailsOverflowGapPadding`:

```kotlin
private const val AlcoholUnitProgressRingAnimationDurationMillis = 800
private val AlcoholUnitProgressRingAnimationEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
```

- [ ] **Step 3: Replace the shared ring composable with the animated implementation**

Replace the current `AlcoholUnitProgressRing` composable with:

```kotlin
@Composable
internal fun AlcoholUnitProgressRing(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.inverseSurface,
    strokeWidth: Dp = 5.dp,
    overflowGapPaddingFraction: Float = AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction,
    animateProgress: Boolean = false
) {
    val targetRatio = calculateAlcoholUnitIndicatorRatio(
        unitCount = alcoholUnitLevel.unitCount,
        limit = alcoholUnitLevel.limit
    )
    val animatedRatio = remember { Animatable(0f) }

    LaunchedEffect(animateProgress, targetRatio) {
        if (animateProgress) {
            animatedRatio.animateTo(
                targetValue = targetRatio,
                animationSpec = tween(
                    durationMillis = AlcoholUnitProgressRingAnimationDurationMillis,
                    easing = AlcoholUnitProgressRingAnimationEasing
                )
            )
        } else {
            animatedRatio.snapTo(targetRatio)
        }
    }

    val ratio = resolveAlcoholUnitIndicatorDrawRatio(
        targetRatio = targetRatio,
        animatedRatio = animatedRatio.value,
        animateProgress = animateProgress
    )
    val levelColor = alcoholUnitLevelIndicatorColor(alcoholUnitLevel)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.99f }
    ) {
        drawAlcoholUnitProgressRing(
            ratio = ratio,
            levelColor = levelColor,
            trackColor = trackColor,
            strokeWidth = strokeWidth.toPx(),
            overflowGapPaddingFraction = overflowGapPaddingFraction
        )
    }
}
```

- [ ] **Step 4: Run the targeted test to catch compile regressions**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: PASS for all tests in `AlcoholUnitProgressRingTest`.

- [ ] **Step 5: Commit the shared animation implementation**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt
git commit -m "feat: add opt-in progress ring animation"
```

---

### Task 3: Enable Animation Only On Approved Ring Usages

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`

- [ ] **Step 1: Opt in Home summary rings**

In `DrinksSummaryCard.kt`, update the `AlcoholUnitProgressRing` call inside the card row to:

```kotlin
                AlcoholUnitProgressRing(
                    modifier = Modifier.size(54.dp),
                    alcoholUnitLevel = alcoholUnitLevel,
                    animateProgress = true
                )
```

- [ ] **Step 2: Opt in the Calendar month summary ring**

In `CalendarScreen.kt`, update the `AlcoholUnitProgressRing` call inside `MonthConsumptionIndicator` to:

```kotlin
        AlcoholUnitProgressRing(
            alcoholUnitLevel = alcoholUnitLevel,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = MonthConsumptionIndicatorStrokeWidth,
            modifier = Modifier.fillMaxSize(),
            animateProgress = true
        )
```

- [ ] **Step 3: Keep Calendar day-cell rings static**

Confirm the `AlcoholUnitProgressRing` call inside `DayCell` still has no `animateProgress` argument:

```kotlin
                AlcoholUnitProgressRing(
                    modifier = Modifier.matchParentSize(),
                    alcoholUnitLevel = it,
                    strokeWidth = 3.dp,
                )
```

- [ ] **Step 4: Opt in the Day Details large ring**

In `DayConsumptionIndicator.kt`, update the `AlcoholUnitProgressRing` call to:

```kotlin
        AlcoholUnitProgressRing(
            alcoholUnitLevel = alcoholUnitLevel,
            trackColor = trackColor,
            strokeWidth = IndicatorStrokeWidth,
            modifier = Modifier
                .fillMaxSize(),
            animateProgress = true
        )
```

- [ ] **Step 5: Confirm only three call sites opt in**

Run:

```bash
rg -n "animateProgress = true|AlcoholUnitProgressRing\\(" app/src/main/java/com/mgruchala/drinkwise/presentation
```

Expected: `animateProgress = true` appears in exactly these files:

```text
app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt
app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt
app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt
```

Expected: the `DayCell` ring in `CalendarScreen.kt` still calls `AlcoholUnitProgressRing` without `animateProgress = true`.

- [ ] **Step 6: Run app unit tests**

Run:

```bash
./gradlew test
```

Expected: PASS for all unit tests.

- [ ] **Step 7: Commit the opt-in call sites**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt
git commit -m "feat: animate selected progress rings"
```

---

### Task 4: Verify UI Behavior With Manual And Maestro Checks

**Files:**
- No code files should change in this task.
- Existing verification flows: `maestro/flows/home-smoke.yaml`, `maestro/flows/calendar-day-details.yaml`

- [ ] **Step 1: Run the Home smoke flow when an emulator or device is available**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/home-smoke.yaml
```

Expected: PASS, with a `home-smoke` screenshot in the printed `artifacts/maestro/<timestamp>` directory.

If no emulator/device can be used in the execution environment, record this as SKIP with the exact missing dependency or startup error.

- [ ] **Step 2: Run the Calendar and Day Details flow when an emulator or device is available**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
```

Expected: PASS, with `calendar-month-summary` and `calendar-day-details-design` screenshots in the printed `artifacts/maestro/<timestamp>` directory.

If no emulator/device can be used in the execution environment, record this as SKIP with the exact missing dependency or startup error.

- [ ] **Step 3: Manually inspect Home animation behavior**

Open the debug app and verify:

```text
1. Home initially shows Today, This Week, and This Month ring progress drawing from empty to final state.
2. Expand Today, switch between Since midnight and Last 24h, and confirm the ring animates from the current displayed state to the new state.
3. Expand This Week, switch between This week and Last 7 days, and confirm the same transition behavior.
4. Expand This Month, switch between This month and Last 30 days, and confirm the same transition behavior.
5. The text values update immediately and do not count up.
```

- [ ] **Step 4: Manually inspect Calendar and Day Details animation behavior**

Open the debug app and verify:

```text
1. Calendar month summary ring draws from empty to final state when the month summary first appears.
2. Calendar day-cell rings do not animate.
3. Over-limit month summary preserves the existing full base lap, rounded cap, overflow gap, and overflow lap.
4. Opening a day details page animates the large ring from empty to final state.
5. Over-limit day details preserves the existing full base lap, rounded cap, overflow gap, overflow lap, and +x label.
6. Center text and accessibility content describe the final values, not animation frames.
```

- [ ] **Step 5: Capture final git state**

Run:

```bash
git status --short
```

Expected: no modified tracked files. Generated Maestro artifacts may appear under `artifacts/` and should remain untracked unless explicitly requested.
