# Fix Scaffold Padding for Bottom Navigation Screens

## Summary

Apply the SettingsScreen innerPadding pattern to all bottom navigation screens so content doesn't draw under the status bar.

## Background

- The root `Scaffold` in `AppNavigation.kt:37` uses `contentWindowInsets = WindowInsets(0)` to avoid double-applied system bar insets (fixed DayDetails)
- As a consequence, each bottom navigation screen must handle status bar padding via its own Scaffold's `innerPadding`
- SettingsScreen already implements this correctly; other screens do not

## Files to Modify

### 1. HomeScreen.kt
**Path:** `app/src/main/java/com/mgruchala/drinkwise/presentation/home/HomeScreen.kt`

**Current issue:** Scaffold ignores innerPadding (line 82), has `@SuppressLint` (line 46)

**Changes:**
- Line 82: Change `content = {` to `content = { innerPadding ->`
- Line 83-86: Add `.padding(innerPadding)` before `.padding(horizontal = 16.dp, vertical = 8.dp)`
- Remove `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` import and annotation (lines 3, 46)

### 2. CalendarScreen.kt
**Path:** `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`

**Current issue:** Scaffold ignores innerPadding (line 94), has `@SuppressLint` (line 81)

**Changes:**
- Line 94: Change `Scaffold {` to `Scaffold { innerPadding ->`
- Line 95-98: Add `.padding(innerPadding)` before `.padding(horizontal = 8.dp)`
- Remove `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` import and annotation (lines 3, 81)

### 3. AlcoholUnitsCalculator.kt
**Path:** `app/src/main/java/com/mgruchala/drinkwise/presentation/calculator/AlcoholUnitsCalculator.kt`

**Current issue:** No Scaffold at all - screen may clip under status bar

**Changes:**
- Wrap `AlcoholCalculatorView` content in a Scaffold
- Apply innerPadding to the `AlcoholCalculatorContent` modifier

**New structure:**
```kotlin
@Composable
fun AlcoholCalculatorView(
    viewModel: AlcoholUnitsCalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    Scaffold { innerPadding ->
        AlcoholCalculatorContent(
            modifier = modifier.padding(innerPadding),
            state = state,
            ...
        )
    }
}
```

## Detailed Code Changes

### HomeScreen.kt

**Remove import (line 3):**
```kotlin
// DELETE: import android.annotation.SuppressLint
```

**Remove annotation (line 46):**
```kotlin
// DELETE: @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
```

**Update Scaffold content (lines 82-86):**
```kotlin
// BEFORE:
content = {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),

// AFTER:
content = { innerPadding ->
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
```

### CalendarScreen.kt

**Remove import (line 3):**
```kotlin
// DELETE: import android.annotation.SuppressLint
```

**Remove annotation (line 81):**
```kotlin
// DELETE: @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
```

**Update Scaffold (lines 94-99):**
```kotlin
// BEFORE:
Scaffold {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
    ) {

// AFTER:
Scaffold { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 8.dp)
    ) {
```

### AlcoholUnitsCalculator.kt

**Add imports:**
```kotlin
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
```

**Update AlcoholCalculatorView (lines 40-55):**
```kotlin
// BEFORE:
@Composable
fun AlcoholCalculatorView(
    viewModel: AlcoholUnitsCalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    AlcoholCalculatorContent(
        modifier = modifier,
        state = state,
        onQuantityChanged = viewModel::onQuantityChanged,
        onPercentageChanged = viewModel::onPercentageChanged,
        onNumberDecrement = viewModel::onDecrement,
        onNumberIncrement = viewModel::onIncrement,
        isInDialog = false
    )
}

// AFTER:
@Composable
fun AlcoholCalculatorView(
    viewModel: AlcoholUnitsCalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    Scaffold { innerPadding ->
        AlcoholCalculatorContent(
            modifier = modifier.padding(innerPadding),
            state = state,
            onQuantityChanged = viewModel::onQuantityChanged,
            onPercentageChanged = viewModel::onPercentageChanged,
            onNumberDecrement = viewModel::onDecrement,
            onNumberIncrement = viewModel::onIncrement,
            isInDialog = false
        )
    }
}
```

## SettingsScreen Note

SettingsScreen already handles innerPadding correctly at lines 59, 63. The `@SuppressLint` annotation on line 40 can be removed since innerPadding is now used. This is optional cleanup.

## Verification

1. Build the app: `./gradlew assembleDebug`
2. Run on device/emulator with edge-to-edge display
3. Verify each bottom navigation screen:
   - **Home**: Header content not clipped under status bar
   - **Calendar**: Month navigation header not clipped under status bar
   - **Calculator**: Content not clipped under status bar
   - **Settings**: Already works correctly (reference implementation)
4. Navigate to DayDetails and back - ensure no double-inset padding appears
5. Verify no lint warnings about unused Scaffold padding parameter
