# Alcohol Unit Indicator Overflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reuse the Day Details overflow ring behavior across Home, Calendar, and Day Details while preserving the compact Home and Calendar layouts.

**Architecture:** Extract progress math, color selection, and Canvas ring drawing into `presentation/common`. Keep `AlcoholUnitLevelProgressIndicator` as the compact public wrapper used by Home and Calendar, and update `DayConsumptionIndicator` to delegate only the ring rendering to the shared implementation while keeping its rich text and over-limit label.

**Tech Stack:** Kotlin, Jetpack Compose Canvas, Material 3 color scheme, JUnit 5, Gradle, Maestro for Android UI verification.

---

## File Structure

- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt`
  - Owns safe limit calculation, ratio calculation, base progress calculation, overflow progress calculation, level color selection, and shared Canvas drawing.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitLevelProgressIndicator.kt`
  - Keeps the current compact API and delegates to `AlcoholUnitProgressRing`.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`
  - Removes duplicated Canvas/progress helpers and delegates ring drawing to `AlcoholUnitProgressRing`.
- Create `app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt`
  - Covers the shared progress math and defensive limit handling.
- Delete `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicatorOverflowTest.kt`
  - Replaced by common helper tests.
- No Home or Calendar layout files need behavior changes because they already call `AlcoholUnitLevelProgressIndicator`.

---

### Task 1: Move Progress Math Tests To Common

**Files:**
- Create: `app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt`
- Later delete: `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicatorOverflowTest.kt`

- [ ] **Step 1: Write the failing common test file**

Create `app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AlcoholUnitProgressRingTest {

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
    }

    @Test
    fun `safe limit keeps positive configured limits`() {
        val limit = calculateAlcoholUnitIndicatorSafeLimit(limit = 4f)

        assertEquals(4f, limit, FLOAT_TOLERANCE)
    }

    @Test
    fun `safe limit clamps zero limits`() {
        val limit = calculateAlcoholUnitIndicatorSafeLimit(limit = 0f)

        assertEquals(0.1f, limit, FLOAT_TOLERANCE)
    }

    @Test
    fun `safe limit clamps negative limits`() {
        val limit = calculateAlcoholUnitIndicatorSafeLimit(limit = -2f)

        assertEquals(0.1f, limit, FLOAT_TOLERANCE)
    }

    @Test
    fun `ratio uses the safe limit`() {
        val ratio = calculateAlcoholUnitIndicatorRatio(unitCount = 4f, limit = 0f)

        assertEquals(40f, ratio, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress follows consumption up to the limit`() {
        val progress = calculateAlcoholUnitIndicatorBaseProgress(ratio = 0.41f)

        assertEquals(0.41f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress is capped at the limit`() {
        val progress = calculateAlcoholUnitIndicatorBaseProgress(ratio = 1.14f)

        assertEquals(1f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress does not render negative consumption`() {
        val progress = calculateAlcoholUnitIndicatorBaseProgress(ratio = -0.25f)

        assertEquals(0f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress is absent up to the limit`() {
        val belowLimitProgress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 0.41f)
        val atLimitProgress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 1f)

        assertEquals(0f, belowLimitProgress, FLOAT_TOLERANCE)
        assertEquals(0f, atLimitProgress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress follows the current lap after the limit`() {
        val progress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 1.14f)

        assertEquals(0.14f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress preserves the current lap for large overages`() {
        val progress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 7.41f)

        assertEquals(0.41f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress renders a full lap for exact over-limit cycles`() {
        val progress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 3f)

        assertEquals(1f, progress, FLOAT_TOLERANCE)
    }
}
```

- [ ] **Step 2: Run the targeted test to verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: FAIL with unresolved references for `calculateAlcoholUnitIndicatorSafeLimit`, `calculateAlcoholUnitIndicatorRatio`, `calculateAlcoholUnitIndicatorBaseProgress`, and `calculateAlcoholUnitIndicatorOverflowProgress`.

- [ ] **Step 3: Add the minimal shared progress helper implementation**

Create `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.common

import kotlin.math.floor

private const val MinimumAlcoholUnitIndicatorLimit = 0.1f

internal fun calculateAlcoholUnitIndicatorSafeLimit(limit: Float): Float {
    return limit.coerceAtLeast(MinimumAlcoholUnitIndicatorLimit)
}

internal fun calculateAlcoholUnitIndicatorRatio(unitCount: Float, limit: Float): Float {
    return unitCount / calculateAlcoholUnitIndicatorSafeLimit(limit)
}

internal fun calculateAlcoholUnitIndicatorBaseProgress(ratio: Float): Float {
    return ratio.coerceIn(0f, 1f)
}

internal fun calculateAlcoholUnitIndicatorOverflowProgress(ratio: Float): Float {
    if (ratio <= 1f) {
        return 0f
    }

    val currentLapProgress = ratio - floor(ratio)
    return if (currentLapProgress == 0f) 1f else currentLapProgress
}
```

- [ ] **Step 4: Run the targeted test to verify it passes**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: PASS for all tests in `AlcoholUnitProgressRingTest`.

- [ ] **Step 5: Commit the shared helper tests and implementation**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt
git commit -m "test: cover shared alcohol indicator progress"
```

---

### Task 2: Extract The Shared Canvas Ring Renderer

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt`

- [ ] **Step 1: Replace the helper-only file with the full shared renderer**

Update `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt` to this complete content:

```kotlin
package com.mgruchala.drinkwise.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

private const val StartAngleDegrees = -90f
private const val MinimumAlcoholUnitIndicatorLimit = 0.1f

internal fun calculateAlcoholUnitIndicatorSafeLimit(limit: Float): Float {
    return limit.coerceAtLeast(MinimumAlcoholUnitIndicatorLimit)
}

internal fun calculateAlcoholUnitIndicatorRatio(unitCount: Float, limit: Float): Float {
    return unitCount / calculateAlcoholUnitIndicatorSafeLimit(limit)
}

internal fun calculateAlcoholUnitIndicatorBaseProgress(ratio: Float): Float {
    return ratio.coerceIn(0f, 1f)
}

internal fun calculateAlcoholUnitIndicatorOverflowProgress(ratio: Float): Float {
    if (ratio <= 1f) {
        return 0f
    }

    val currentLapProgress = ratio - floor(ratio)
    return if (currentLapProgress == 0f) 1f else currentLapProgress
}

internal fun alcoholUnitLevelIndicatorColor(alcoholUnitLevel: AlcoholUnitLevel): Color {
    return when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }
}

@Composable
internal fun AlcoholUnitProgressRing(
    alcoholUnitLevel: AlcoholUnitLevel,
    trackColor: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
    overflowGapPadding: Dp = 4.dp
) {
    val ratio = calculateAlcoholUnitIndicatorRatio(
        unitCount = alcoholUnitLevel.unitCount,
        limit = alcoholUnitLevel.limit
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
            overflowGapPadding = overflowGapPadding.toPx()
        )
    }
}

private fun DrawScope.drawAlcoholUnitProgressRing(
    ratio: Float,
    levelColor: Color,
    trackColor: Color,
    strokeWidth: Float,
    overflowGapPadding: Float
) {
    val outerRadius = min(size.width, size.height) / 2f - strokeWidth / 2f
    if (outerRadius <= 0f) {
        return
    }

    if (ratio <= 1f) {
        drawCircle(
            color = trackColor,
            radius = outerRadius,
            style = Stroke(width = strokeWidth)
        )

        drawAlcoholUnitProgress(
            color = levelColor,
            radius = outerRadius,
            strokeWidth = strokeWidth,
            progress = calculateAlcoholUnitIndicatorBaseProgress(ratio)
        )
    } else {
        drawCircle(
            color = levelColor,
            radius = outerRadius,
            style = Stroke(width = strokeWidth)
        )

        val overflowProgress = calculateAlcoholUnitIndicatorOverflowProgress(ratio)
        if (overflowProgress > 0f && overflowProgress < 1f) {
            val endAngleDegrees = StartAngleDegrees + (360f * overflowProgress)
            val endAngleRad = Math.toRadians(endAngleDegrees.toDouble())
            val headX = center.x + outerRadius * cos(endAngleRad).toFloat()
            val headY = center.y + outerRadius * sin(endAngleRad).toFloat()

            drawCircle(
                color = Color.Black,
                radius = (strokeWidth / 2f) + overflowGapPadding,
                center = Offset(headX, headY),
                blendMode = BlendMode.Clear
            )

            drawAlcoholUnitProgress(
                color = levelColor,
                radius = outerRadius,
                strokeWidth = strokeWidth,
                progress = overflowProgress
            )
        }
    }
}

private fun DrawScope.drawAlcoholUnitProgress(
    color: Color,
    radius: Float,
    strokeWidth: Float,
    progress: Float
) {
    when {
        progress >= 1f -> drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        progress > 0f -> drawArc(
            color = color,
            startAngle = StartAngleDegrees,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
```

- [ ] **Step 2: Run the targeted helper tests after adding Compose imports**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: PASS. This also checks that the shared renderer file compiles.

- [ ] **Step 3: Commit the shared renderer extraction**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt
git commit -m "feat: add shared alcohol indicator ring"
```

---

### Task 3: Migrate The Compact Indicator To The Shared Renderer

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitLevelProgressIndicator.kt`

- [ ] **Step 1: Replace Material `CircularProgressIndicator` usage**

Update `app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitLevelProgressIndicator.kt` to this complete content:

```kotlin
package com.mgruchala.drinkwise.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel

@Composable
fun AlcoholUnitLevelProgressIndicator(
    modifier: Modifier,
    strokeWidth: Dp = 5.dp,
    alcoholUnitLevel: AlcoholUnitLevel
) {
    AlcoholUnitProgressRing(
        alcoholUnitLevel = alcoholUnitLevel,
        trackColor = MaterialTheme.colorScheme.inverseSurface,
        strokeWidth = strokeWidth,
        modifier = modifier
    )
}
```

- [ ] **Step 2: Run the targeted helper tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: PASS.

- [ ] **Step 3: Confirm Home and Calendar still call the compact API unchanged**

Run:

```bash
rg -n "AlcoholUnitLevelProgressIndicator" app/src/main/java/com/mgruchala/drinkwise/presentation
```

Expected output includes these call sites:

```text
app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt:...
app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt:...
```

No Home or Calendar layout edits are needed in this task.

- [ ] **Step 4: Commit the compact indicator migration**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitLevelProgressIndicator.kt
git commit -m "feat: use overflow ring for compact alcohol indicators"
```

---

### Task 4: Refactor Day Details To Use The Shared Renderer

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`
- Delete: `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicatorOverflowTest.kt`

- [ ] **Step 1: Update Day Details imports**

In `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`, remove these imports:

```kotlin
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
```

Add these imports:

```kotlin
import com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRing
import com.mgruchala.drinkwise.presentation.common.calculateAlcoholUnitIndicatorRatio
import com.mgruchala.drinkwise.presentation.common.calculateAlcoholUnitIndicatorSafeLimit
```

- [ ] **Step 2: Remove the Day Details-only progress helpers**

Delete these declarations from `DayConsumptionIndicator.kt`:

```kotlin
private const val START_ANGLE_DEGREES = -90f
```

```kotlin
internal fun calculateConsumptionIndicatorBaseProgress(ratio: Float): Float {
    return ratio.coerceIn(0f, 1f)
}

internal fun calculateConsumptionIndicatorOverflowProgress(ratio: Float): Float {
    if (ratio <= 1f) {
        return 0f
    }

    val currentLapProgress = ratio - floor(ratio)
    return if (currentLapProgress == 0f) 1f else currentLapProgress
}
```

- [ ] **Step 3: Use the shared safe limit and ratio helpers**

Inside `DayConsumptionIndicator`, replace:

```kotlin
val limit = alcoholUnitLevel.limit.coerceAtLeast(0.1f)
val ratio = consumed / limit
```

with:

```kotlin
val limit = calculateAlcoholUnitIndicatorSafeLimit(alcoholUnitLevel.limit)
val ratio = calculateAlcoholUnitIndicatorRatio(
    unitCount = consumed,
    limit = alcoholUnitLevel.limit
)
```

- [ ] **Step 4: Remove duplicated color selection**

Delete this block from `DayConsumptionIndicator`:

```kotlin
val levelColor = when (alcoholUnitLevel) {
    is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
    is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
    is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
}
```

Keep:

```kotlin
val textColor = MaterialTheme.colorScheme.onSurface
val trackColor = MaterialTheme.colorScheme.surfaceVariant
```

- [ ] **Step 5: Replace the inline Canvas with the shared ring**

Inside the `Box` content, replace the entire `Canvas(...) { ... }` block with:

```kotlin
AlcoholUnitProgressRing(
    alcoholUnitLevel = alcoholUnitLevel,
    trackColor = trackColor,
    strokeWidth = IndicatorStrokeWidth,
    modifier = Modifier.fillMaxSize()
)
```

- [ ] **Step 6: Delete the private Day Details draw helper**

Delete this full function from `DayConsumptionIndicator.kt`:

```kotlin
private fun DrawScope.drawConsumptionProgress(
    color: Color,
    radius: Float,
    strokeWidth: Float,
    progress: Float
) {
    when {
        progress >= 1f -> drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        progress > 0f -> drawArc(
            color = color,
            startAngle = START_ANGLE_DEGREES,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
```

- [ ] **Step 7: Delete the old Day Details overflow test**

Delete:

```text
app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicatorOverflowTest.kt
```

The replacement coverage is now in:

```text
app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt
```

- [ ] **Step 8: Run the targeted common test**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRingTest"
```

Expected: PASS.

- [ ] **Step 9: Search for removed helper references**

Run:

```bash
rg -n "calculateConsumptionIndicator|START_ANGLE_DEGREES|drawConsumptionProgress" app/src
```

Expected: no output.

- [ ] **Step 10: Commit the Day Details refactor**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicatorOverflowTest.kt
git commit -m "refactor: share day details alcohol indicator ring"
```

---

### Task 5: Verify App-Wide Behavior

**Files:**
- No source files should be modified in this task.

- [ ] **Step 1: Run all JVM tests**

Run:

```bash
./gradlew test
```

Expected: PASS.

- [ ] **Step 2: Run a debug compile check without release signing**

Run:

```bash
./gradlew assembleDebug
```

Expected: PASS. This avoids the release variant and local signing requirements.

- [ ] **Step 3: Verify existing call sites still use the compact indicator**

Run:

```bash
rg -n "AlcoholUnitLevelProgressIndicator|AlcoholUnitProgressRing|calculateAlcoholUnitIndicator" app/src/main/java app/src/test/java
```

Expected:

```text
app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitLevelProgressIndicator.kt
app/src/main/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRing.kt
app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt
app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt
app/src/main/java/com/mgruchala/drinkwise/presentation/home/DrinksSummaryCard.kt
app/src/test/java/com/mgruchala/drinkwise/presentation/common/AlcoholUnitProgressRingTest.kt
```

- [ ] **Step 4: Run Home Maestro verification on a connected device or emulator**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/home-smoke.yaml
```

Expected: PASS and screenshot `home-smoke` captured by the flow.

- [ ] **Step 5: Run Calendar/Day Details Maestro verification on a connected device or emulator**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
```

Expected: PASS and screenshot `calendar-day-details-design` captured by the flow.

- [ ] **Step 6: Record emulator/device blocker when Maestro cannot run**

If Step 4 or Step 5 fails because no emulator or device is connected, run:

```bash
adb devices
```

Expected: output has no device in `device` state. Record Maestro as blocked by missing Android device in the final implementation summary.

- [ ] **Step 7: Commit verification-only fixes if compilation exposed any**

If Steps 1-5 required source edits, commit those edits:

```bash
git add app/src/main/java app/src/test/java
git commit -m "fix: polish shared alcohol indicator migration"
```

Expected: create this commit only when verification required additional source changes.

---

## Plan Self-Review Checklist

- Spec coverage: The plan extracts common ring logic, migrates compact Home/Calendar indicators through their existing wrapper, preserves Day Details content, moves tests, and includes JVM plus Maestro verification.
- Placeholder scan: The plan contains concrete file paths, commands, expected results, code snippets, and commit messages.
- Type consistency: Helper names are consistent across tasks: `calculateAlcoholUnitIndicatorSafeLimit`, `calculateAlcoholUnitIndicatorRatio`, `calculateAlcoholUnitIndicatorBaseProgress`, `calculateAlcoholUnitIndicatorOverflowProgress`, `alcoholUnitLevelIndicatorColor`, and `AlcoholUnitProgressRing`.
