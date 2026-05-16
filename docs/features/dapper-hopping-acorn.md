# Day Details - Visual Improvements Plan

This plan addresses 4 issues identified in `docs/features/day-details/things_to_improve.md` after the initial implementation.

---

## Issues Summary

| # | Issue | File | Complexity |
|---|-------|------|------------|
| 1 | Animation not visible on screen entry | ConsumptionCircle.kt | Medium |
| 2 | Circle stacking order wrong (partial ring should be outermost) | ConsumptionCircle.kt | Medium |
| 3 | Circles too close to text inside | ConsumptionCircle.kt | Low |
| 4 | Top bar too large | DayDetailsScreen.kt | Low |

---

## Issue 1: Animation Not Visible on Screen Entry

**Problem**: Animation runs during composition before the user can see it (during Loading→Content transition).

**Solution**: Add a delayed animation trigger that ensures visibility before starting.

**Changes to `ConsumptionCircle.kt`**:
```kotlin
// Add state to track animation trigger
var hasAnimated by remember { mutableStateOf(false) }

// Change Animatable creation to NOT key on targetProgress
val animatable = remember { Animatable(0f) }

// Trigger animation only after hasAnimated is true
LaunchedEffect(targetProgress, hasAnimated) {
    if (animate && hasAnimated) {
        animatable.animateTo(targetValue = targetProgress, ...)
    }
}

// Delay animation start to ensure visibility
LaunchedEffect(Unit) {
    if (animate) {
        delay(100)
        hasAnimated = true
    }
}
```

---

## Issue 2: Circle Stacking Order Wrong

**Problem**: Currently incomplete/partial ring is drawn INSIDE. User wants it OUTSIDE.

**Current behavior at 150%**:
- Outer ring: 100% filled
- Inner ring: 50% filled (partial)

**Desired behavior at 150%**:
- Outer ring: 50% filled (partial)
- Inner ring: 100% filled

**Solution**: Reverse the padding calculation so partial ring gets 0 padding (outermost).

**Changes to `ConsumptionCircle.kt`**:
```kotlin
animatedProgresses.forEachIndexed { index, animatable ->
    // Reverse: partial ring (last in list) becomes outermost (0 padding)
    val reversedIndex = animatedProgresses.size - 1 - index
    val ringPadding = (reversedIndex * RING_PADDING_DP).dp

    // Color based on original index (0 = base, >0 = red overflow)
    val ringColor = if (index > 0) AlcoholUnitLevelHigh else baseColor
    val strokeWidth = calculateStrokeWidth(reversedIndex)
    // ...
}
```

---

## Issue 3: Circles Too Close to Text

**Problem**: With multiple rings, innermost ring gets too close to centered text.

**Solution**: Add dynamic padding to text Column based on number of rings.

**Changes to `ConsumptionCircle.kt`**:
```kotlin
// Calculate padding based on ring count
val totalRingInset = (numberOfRings - 1) * RING_PADDING_DP + 12 // stroke width
val textPadding = (totalRingInset + 16).dp // 16dp extra spacing

Column(
    modifier = Modifier.padding(textPadding),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(...) // "4.2 / 7"
    Text(...) // "units"
}
```

---

## Issue 4: Top Bar Too Large

**Problem**: `LargeTopAppBar` (~152dp) is too tall for this screen.

**Solution**: Replace with regular `TopAppBar` (~64dp).

**Changes to `DayDetailsScreen.kt`**:

1. Replace `LargeTopAppBar` with `TopAppBar`
2. Remove `exitUntilCollapsedScrollBehavior`
3. Remove `collapsedFraction` logic
4. Remove `nestedScroll` modifier from Scaffold
5. Show date + relative time in compact single-line or two-line format

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = DateFormatter.formatFullDate(currentDate),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                    Text(
                        text = DateFormatter.formatRelativeTime(currentDate, context),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = { /* back button */ }
        )
    }
)
```

---

## Implementation Order

1. **Issue 4: Top Bar** - Simplest, quick visual win
2. **Issue 3: Text Spacing** - Isolated change to ConsumptionCircle
3. **Issue 2: Stacking Order** - Changes ring rendering logic
4. **Issue 1: Animation** - Most complex, test after other changes

---

## Files to Modify

| File | Issues |
|------|--------|
| `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/ConsumptionCircle.kt` | 1, 2, 3 |
| `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt` | 4 |

---

## Verification

After implementation:

1. **Build check**: `./gradlew assembleDebug`
2. **Tests**: `./gradlew test`
3. **Manual testing**:
   - Navigate to Day Details from Calendar - animation should play
   - Swipe between days - animation should play on each
   - Rotate device - animation should replay
   - Check over-limit display (150%+) - partial ring should be outermost
   - Check text spacing with 2+ rings - adequate gap from innermost ring
   - Top bar should be compact (~64dp height)
