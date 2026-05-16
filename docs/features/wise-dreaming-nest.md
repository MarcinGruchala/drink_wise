# Implementation Plan: Subplans 6, 7, 8 - Day Details Feature

This plan covers Drink List Item component, Basic Screen Layout, and State Handling.

---

## Subplan 6: UI Component - Drink List Item

### Goal
Create a drink list item card component and volume formatter utility.

### Files to Create

#### 1. `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/VolumeFormatter.kt`

Utility function to format volume context-aware:
- < 1000ml: "330 ml", "500 ml"
- >= 1000ml: "1 L", "1.5 L"

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

fun formatVolume(volumeMl: Int): String {
    return if (volumeMl >= 1000) {
        val liters = volumeMl / 1000f
        if (liters == liters.toLong().toFloat()) {
            "${liters.toLong()} L"
        } else {
            "${"%.1f".format(liters)} L"
        }
    } else {
        "$volumeMl ml"
    }
}
```

#### 2. `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`

**Note:** DrinkEntity has only: `uid`, `quantity` (ml), `alcoholContent` (ABV%), `timestamp`. No drink type/name exists.

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

@Composable
fun DrinkListItem(
    drink: DrinkEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Layout:
    // [Icon] | Volume • ABV% • X.X units
    //        | Time (formatted from timestamp)

    // Use calculateAlcoholUnits(drink.quantity, drink.alcoholContent) for units
    // Use formatVolume(drink.quantity) for volume display
    // Format timestamp to time string (e.g., "14:30")
}
```

Key implementation details:
- Use `Card` with `CardDefaults.cardColors(containerColor = surfaceContainerHighest)`
- Generic drink icon on the left (use `Icons.Outlined.LocalBar` or similar)
- Primary line: "330 ml • 5.0% • 1.65 units"
- Secondary line: Time formatted from timestamp
- Add ripple effect via `clickable(onClick = onClick)`
- Include `@Preview` composables

---

## Subplan 7: UI - Basic Screen Layout (Portrait)

### Goal
Assemble the Day Details screen with basic portrait layout using LazyColumn.

### Files to Modify

#### `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`

Replace current placeholder with full layout:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DayDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.selectedDate.format(...)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = ...)
                    }
                }
            )
        }
    ) { innerPadding ->
        DayDetailsContent(
            state = state,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun DayDetailsContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier
) {
    // Delegate to specific content based on state
    when {
        state.isLoading -> LoadingContent(modifier)
        state.error != null -> ErrorContent(error = state.error, onRetry = { /* TODO */ }, modifier)
        state.drinks.isEmpty() -> EmptyContent(state, modifier)
        else -> DrinkListContent(state, modifier)
    }
}

@Composable
private fun DrinkListContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Consumption circle as first item
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ConsumptionCircle(
                    consumedUnits = state.totalUnits,
                    limitUnits = state.dailyLimit,
                    alcoholUnitLevel = state.alcoholUnitLevel,
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Drink items
        items(state.drinks, key = { it.uid }) { drink ->
            DrinkListItem(
                drink = drink,
                onClick = { /* Future: edit/delete */ }
            )
        }
    }
}
```

---

## Subplan 8: UI - Empty, Loading, and Error States

### Goal
Handle all screen states properly with dedicated composables.

### Files to Modify

#### `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`

Add state-specific composables:

```kotlin
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.day_details_retry))
        }
    }
}

@Composable
private fun EmptyContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ConsumptionCircle(
            consumedUnits = state.totalUnits,  // Will be 0
            limitUnits = state.dailyLimit,
            alcoholUnitLevel = state.alcoholUnitLevel,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.day_details_no_drinks),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### ViewModel Update (if needed)

Add retry function to `DayDetailsViewModel.kt` if error state needs retry capability:

```kotlin
// Currently the state is derived from flows, so errors would need
// to be handled differently. For now, the error state can show
// the retry button but actual retry logic may be added later.
```

---

## Implementation Order

1. **VolumeFormatter.kt** - Create utility function
2. **DrinkListItem.kt** - Create drink item component
3. **DayDetailsScreen.kt** - Update with full layout and state handling

---

## Files Summary

| Action | File Path |
|--------|-----------|
| Create | `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/VolumeFormatter.kt` |
| Create | `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt` |
| Modify | `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt` |

---

## Verification

1. **Build**: `./gradlew assembleDebug`
2. **Tests**: `./gradlew test`
3. **Manual testing**:
   - Navigate to Calendar, tap a day with drinks → verify list displays
   - Tap a day with no drinks → verify empty state with "No drinks recorded"
   - Check loading spinner appears briefly on navigation
   - Verify volume formatting: 330ml shows as "330 ml", 1500ml shows as "1.5 L"
   - Verify drink items show: volume, ABV%, calculated units, and time
   - Verify scrolling works when many drinks exist
