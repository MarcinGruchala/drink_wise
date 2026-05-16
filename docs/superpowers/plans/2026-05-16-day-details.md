# Day Details Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a calendar-launched Day Details screen that first stops at a reviewable mock-data design checkpoint, then wires real selected-date drink data and verifies the flow with Maestro.

**Architecture:** Keep the existing Navigation Compose and ViewModel patterns. Add a `day_details/{epochDay}` destination, pass the selected date as epoch day, and use a focused `DayDetailsViewModel` after design approval. Fix scaffold inset ownership before adding the details screen so the new top app bar and existing bottom-nav screens behave correctly under edge-to-edge.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Hilt, Room, DataStore Preferences, JUnit 5, Maestro.

---

## File Structure

- Modify `app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppRoute.kt`: add the Day Details route and route builder.
- Modify `app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`: fix root scaffold insets, hide bottom nav for Day Details, add route wiring.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/home/HomeScreen.kt`: consume nested scaffold padding.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`: consume nested scaffold padding, add day click callback and accessibility labels.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/settings/SettingsScreen.kt`: consume nested scaffold padding.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/calculator/AlcoholUnitsCalculator.kt`: wrap the full-screen calculator destination in a scaffold that consumes insets.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`: screen shell, mock-data design content, then final state-backed content.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsState.kt`: final screen state model.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`: final selected-date state.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`: reusable v1a indicator.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`: drink row UI.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayDetailsFormatters.kt`: volume, units, percent, and timestamp formatting.
- Create `app/src/main/java/com/mgruchala/drinkwise/utils/time/LocalDateRange.kt`: testable local-day timestamp range helper.
- Modify `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt`: add `getDrinksForDate`.
- Modify `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt`: implement date filtering via existing DAO range query.
- Modify `app/src/main/res/values/strings.xml`: add all Day Details and calendar day accessibility strings.
- Create `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayDetailsFormattersTest.kt`: formatter tests.
- Create `app/src/test/java/com/mgruchala/drinkwise/utils/time/LocalDateRangeTest.kt`: date range tests.
- Create `maestro/flows/calendar-day-details.yaml`: end-to-end Calendar -> Day Details -> back verification.

---

## Task 1: Scaffold Insets Fix

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/calculator/AlcoholUnitsCalculator.kt`

- [ ] **Step 1: Confirm current scaffold suppressions**

Run:

```bash
rg -n "UnusedMaterial3ScaffoldPaddingParameter|Scaffold\\(" app/src/main/java/com/mgruchala/drinkwise
```

Expected: `HomeScreen.kt`, `CalendarScreen.kt`, and `SettingsScreen.kt` show unused scaffold padding suppressions or ignored padding.

- [ ] **Step 2: Fix root scaffold inset ownership**

In `AppNavigation.kt`, add:

```kotlin
import androidx.compose.foundation.layout.WindowInsets
```

Change the root scaffold to:

```kotlin
Scaffold(
    contentWindowInsets = WindowInsets(0),
    bottomBar = {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        NavigationBar {
            bottomNavigationItems.forEach { bottomNavigationItem ->
                val selected = currentRoute == bottomNavigationItem.route.name
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(bottomNavigationItem.route.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        val iconAssetId = if (selected) {
                            bottomNavigationItem.activeIconAssetId
                        } else {
                            bottomNavigationItem.inactiveIconAssetId
                        }
                        Icon(
                            painter = painterResource(iconAssetId),
                            contentDescription = stringResource(bottomNavigationItem.nameRes)
                        )
                    },
                    label = {
                        Text(stringResource(bottomNavigationItem.nameRes))
                    }
                )
            }
        }
    }
)
```

- [ ] **Step 3: Fix Home scaffold padding**

In `HomeScreen.kt`, remove:

```kotlin
import android.annotation.SuppressLint
```

Remove the `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` annotation from `HomeScreenContent`.

Change the scaffold content lambda to:

```kotlin
content = { innerPadding ->
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DrinksSummaryCard(
            period = DrinkSummaryCardPeriod.DAILY,
            alcoholUnitLevel = state.todayAlcoholUnitLevel,
            currentMode = state.dailySummaryCalculationMode,
            onModeChange = { calculationMode ->
                updateDailySummaryPeriod(calculationMode)
            }
        )
        DrinksSummaryCard(
            period = DrinkSummaryCardPeriod.WEEKLY,
            alcoholUnitLevel = state.weekAlcoholUnitLevel,
            currentMode = state.weeklySummaryCalculationMode,
            onModeChange = { calculationMode ->
                updateWeeklySummaryPeriod(calculationMode)
            }
        )
        DrinksSummaryCard(
            period = DrinkSummaryCardPeriod.MONTHLY,
            alcoholUnitLevel = state.monthAlcoholUnitLevel,
            currentMode = state.monthlySummaryCalculationMode,
            onModeChange = { calculationMode ->
                updateMonthlySummaryPeriod(calculationMode)
            }
        )
    }
}
```

- [ ] **Step 4: Fix Calendar scaffold padding**

In `CalendarScreen.kt`, remove:

```kotlin
import android.annotation.SuppressLint
```

Remove `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` from `CalendarScreenContent`.

Change:

```kotlin
Scaffold {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
    ) {
```

to:

```kotlin
Scaffold { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 8.dp)
    ) {
```

- [ ] **Step 5: Fix Settings scaffold padding**

In `SettingsScreen.kt`, remove:

```kotlin
import android.annotation.SuppressLint
```

Remove `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` from `SettingsScreen`.

Change:

```kotlin
) { _ ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
```

to:

```kotlin
) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
```

- [ ] **Step 6: Fix Calculator destination insets**

In `AlcoholUnitsCalculator.kt`, add:

```kotlin
import androidx.compose.material3.Scaffold
```

Change `AlcoholCalculatorView` to:

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
            onQuantityChanged = viewModel::onQuantityChanged,
            onPercentageChanged = viewModel::onPercentageChanged,
            onNumberDecrement = viewModel::onDecrement,
            onNumberIncrement = viewModel::onIncrement,
            isInDialog = false
        )
    }
}
```

- [ ] **Step 7: Run scaffold-focused build check**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 8: Commit scaffold fix**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt app/src/main/java/com/mgruchala/drinkwise/presentation/home/HomeScreen.kt app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt app/src/main/java/com/mgruchala/drinkwise/presentation/settings/SettingsScreen.kt app/src/main/java/com/mgruchala/drinkwise/presentation/calculator/AlcoholUnitsCalculator.kt
git commit -m "fix: handle scaffold insets"
```

---

## Task 2: Day Details Route And Calendar Tap

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppRoute.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add route definition**

In `AppRoute.kt`, append the route object:

```kotlin
data object DayDetails : AppRoute("day_details/{epochDay}") {
    const val ARG_EPOCH_DAY = "epochDay"
    fun createRoute(epochDay: Long): String = "day_details/$epochDay"
}
```

- [ ] **Step 2: Add DayDetails screen shell**

Create `DayDetailsScreen.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(
    selectedDate: LocalDate,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = selectedDate.format(formatter))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.day_details_navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.day_details_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
```

- [ ] **Step 3: Add route strings**

In `strings.xml`, add:

```xml
<!-- Day Details Screen -->
<string name="day_details_title">Day details</string>
<string name="day_details_navigate_back">Navigate back</string>
<string name="calendar_day_content_description">Open details for %1$s</string>
```

- [ ] **Step 4: Wire navigation destination**

In `AppNavigation.kt`, add imports:

```kotlin
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.mgruchala.drinkwise.presentation.daydetails.DayDetailsScreen
import java.time.LocalDate
```

Compute current route and hide bottom nav on Day Details:

```kotlin
val backStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = backStackEntry?.destination?.route
val shouldShowBottomBar = currentRoute != AppRoute.DayDetails.name
```

Wrap `NavigationBar` in:

```kotlin
bottomBar = {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute != AppRoute.DayDetails.name

    if (shouldShowBottomBar) {
        NavigationBar {
            bottomNavigationItems.forEach { bottomNavigationItem ->
                val selected = currentRoute == bottomNavigationItem.route.name
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(bottomNavigationItem.route.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        val iconAssetId = if (selected) {
                            bottomNavigationItem.activeIconAssetId
                        } else {
                            bottomNavigationItem.inactiveIconAssetId
                        }
                        Icon(
                            painter = painterResource(iconAssetId),
                            contentDescription = stringResource(bottomNavigationItem.nameRes)
                        )
                    },
                    label = {
                        Text(stringResource(bottomNavigationItem.nameRes))
                    }
                )
            }
        }
    }
}
```

Change the calendar destination:

```kotlin
composable(AppRoute.Calendar.name) {
    CalendarScreen(
        onDayClick = { date ->
            navController.navigate(AppRoute.DayDetails.createRoute(date.toEpochDay()))
        }
    )
}
```

Add the Day Details destination:

```kotlin
composable(
    route = AppRoute.DayDetails.name,
    arguments = listOf(
        navArgument(AppRoute.DayDetails.ARG_EPOCH_DAY) { type = NavType.LongType }
    )
) { backStackEntry ->
    val epochDay = backStackEntry.arguments?.getLong(AppRoute.DayDetails.ARG_EPOCH_DAY)
        ?: LocalDate.now().toEpochDay()
    DayDetailsScreen(
        selectedDate = LocalDate.ofEpochDay(epochDay),
        onNavigateBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 5: Add calendar click callback**

In `CalendarScreen.kt`, change `CalendarScreen`:

```kotlin
fun CalendarScreen(
    onDayClick: (LocalDate) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
)
```

Pass it into `CalendarScreenContent`:

```kotlin
CalendarScreenContent(
    calendarData = state.calendarData,
    onDayClick = onDayClick
)
```

Change `CalendarScreenContent`:

```kotlin
fun CalendarScreenContent(
    calendarData: Map<YearMonth, List<CalendarDayData>>,
    onDayClick: (LocalDate) -> Unit = {}
)
```

Pass it into `MonthCalendar`:

```kotlin
MonthCalendar(
    month = month,
    originalDays = days,
    onDayClick = onDayClick
)
```

Change `MonthCalendar`, `WeekRow`, and `DayCell` signatures:

```kotlin
fun MonthCalendar(
    month: YearMonth,
    originalDays: List<CalendarDayData>,
    onDayClick: (LocalDate) -> Unit = {}
)

fun WeekRow(
    weekDays: List<CalendarDayData?>,
    onDayClick: (LocalDate) -> Unit = {}
)

fun DayCell(
    dayData: CalendarDayData,
    onClick: () -> Unit = {}
)
```

In `WeekRow`, call:

```kotlin
DayCell(
    dayData = dayData,
    onClick = { onDayClick(dayData.date) }
)
```

- [ ] **Step 6: Make day cells accessible and clickable**

In `CalendarScreen.kt`, add imports:

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.format.FormatStyle
```

Inside `DayCell`, add:

```kotlin
val dayDescriptionFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
val dayContentDescription = stringResource(
    id = R.string.calendar_day_content_description,
    dayData.date.format(dayDescriptionFormatter)
)
```

Update the `Box` modifier:

```kotlin
modifier = Modifier
    .fillMaxWidth()
    .aspectRatio(1f)
    .padding(2.dp)
    .clip(CircleShape)
    .clickable(onClick = onClick)
    .semantics {
        contentDescription = dayContentDescription
    }
    .then(backgroundModifier)
```

- [ ] **Step 7: Build the navigation shell**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 8: Commit route and tap wiring**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppRoute.kt app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt app/src/main/res/values/strings.xml
git commit -m "feat: add day details navigation"
```

---

## Task 3: Design-First Day Details UI With Mock Data

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayConsumptionIndicator.kt`
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayDetailsFormatters.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add design strings**

In `strings.xml`, add:

```xml
<string name="day_details_units_label">units</string>
<string name="day_details_over_limit_amount">+%1$s</string>
<string name="day_details_consumption_indicator_description">%1$s of %2$s units, %3$s percent of daily limit</string>
<string name="day_details_consumption_indicator_over_limit_description">%1$s of %2$s units, %3$s percent of daily limit, over daily limit by %4$s units</string>
<string name="day_details_drinks_section_title">Drinks</string>
<string name="day_details_drink_label">Drink</string>
<string name="day_details_no_drinks">No drinks recorded</string>
<string name="day_details_drink_item_description">%1$s, %2$s alcohol, %3$s units, consumed at %4$s</string>
```

- [ ] **Step 2: Add formatter helpers**

Create `DayDetailsFormatters.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun formatDayDetailsUnits(value: Float): String {
    val roundedToTenth = (value * 10f).roundToInt() / 10f
    return if (roundedToTenth == roundedToTenth.toInt().toFloat()) {
        roundedToTenth.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", roundedToTenth)
    }
}

fun formatDayDetailsVolume(volumeMl: Int): String {
    return if (volumeMl >= 1000) {
        val liters = volumeMl / 1000f
        if (liters == liters.toInt().toFloat()) {
            "${liters.toInt()} L"
        } else {
            String.format(Locale.getDefault(), "%.1f L", liters)
        }
    } else {
        "$volumeMl ml"
    }
}

fun formatDayDetailsAbv(abv: Float): String {
    return String.format(Locale.getDefault(), "%.1f%%", abv)
}

fun formatDayDetailsTime(timestamp: Long): String {
    val localTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
}
```

- [ ] **Step 3: Add v1a consumption indicator**

Create `DayConsumptionIndicator.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow

@Composable
fun DayConsumptionIndicator(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier
) {
    val consumed = alcoholUnitLevel.unitCount
    val limit = alcoholUnitLevel.limit.coerceAtLeast(0.1f)
    val ratio = consumed / limit
    val percent = (ratio * 100f).toInt()
    val consumedText = formatDayDetailsUnits(consumed)
    val limitText = formatDayDetailsUnits(limit)
    val overLimitAmount = (consumed - limit).coerceAtLeast(0f)
    val overLimitText = formatDayDetailsUnits(overLimitAmount)
    val isOverLimit = ratio > 1f

    val levelColor = when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val centerColor = MaterialTheme.colorScheme.surface
    val centerStrokeColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val contentDescription = if (isOverLimit) {
        stringResource(
            id = R.string.day_details_consumption_indicator_over_limit_description,
            consumedText,
            limitText,
            percent.toString(),
            overLimitText
        )
    } else {
        stringResource(
            id = R.string.day_details_consumption_indicator_description,
            consumedText,
            limitText,
            percent.toString()
        )
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = size.minDimension * 0.76f
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val strokeWidth = size.minDimension * 0.08f

            if (isOverLimit) {
                drawCircle(
                    color = levelColor,
                    radius = size.minDimension * 0.38f,
                    center = center
                )
                drawCircle(
                    color = centerColor,
                    radius = size.minDimension * 0.23f,
                    center = center
                )
                drawCircle(
                    color = centerStrokeColor,
                    radius = size.minDimension * 0.23f,
                    center = center,
                    style = Stroke(width = size.minDimension * 0.012f)
                )
                val bubbleCenter = Offset(
                    x = center.x + size.minDimension * 0.29f,
                    y = center.y - size.minDimension * 0.29f
                )
                drawCircle(
                    color = levelColor,
                    radius = size.minDimension * 0.14f,
                    center = bubbleCenter
                )
                drawCircle(
                    color = centerColor,
                    radius = size.minDimension * 0.11f,
                    center = bubbleCenter
                )
            } else {
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = levelColor,
                    startAngle = -90f,
                    sweepAngle = 360f * ratio.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$consumedText of $limitText",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.day_details_units_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (isOverLimit) {
            Text(
                text = stringResource(R.string.day_details_over_limit_amount, overLimitText),
                modifier = Modifier.align(Alignment.TopEnd),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
```

- [ ] **Step 4: Add drink list item**

Create `DrinkListItem.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits

@Composable
fun DrinkListItem(
    drink: DrinkEntity,
    modifier: Modifier = Modifier
) {
    val volume = formatDayDetailsVolume(drink.quantity)
    val abv = formatDayDetailsAbv(drink.alcoholContent)
    val units = formatDayDetailsUnits(calculateAlcoholUnits(drink.quantity, drink.alcoholContent).toFloat())
    val time = formatDayDetailsTime(drink.timestamp)
    val description = stringResource(
        id = R.string.day_details_drink_item_description,
        volume,
        abv,
        units,
        time
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalBar,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.day_details_drink_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$volume • $abv • $units units • $time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider()
    }
}
```

- [ ] **Step 5: Replace the screen shell with mock design content**

In `DayDetailsScreen.kt`, replace the screen shell body with:

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.daydetails.components.DayConsumptionIndicator
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkListItem
```

Add the sample data:

```kotlin
private val sampleDayDetailsDrinks = listOf(
    DrinkEntity(uid = 1, quantity = 500, alcoholContent = 5.2f, timestamp = 1_779_020_400_000L),
    DrinkEntity(uid = 2, quantity = 150, alcoholContent = 13.0f, timestamp = 1_779_016_800_000L),
    DrinkEntity(uid = 3, quantity = 330, alcoholContent = 4.8f, timestamp = 1_779_013_200_000L)
)

private val sampleDayDetailsLevel = AlcoholUnitLevel.fromUnitCount(
    unitCount = 5.6f,
    limit = 4f
)
```

Inside the scaffold content:

```kotlin
LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DayConsumptionIndicator(alcoholUnitLevel = sampleDayDetailsLevel)
        }
    }
    item {
        Text(
            text = stringResource(R.string.day_details_drinks_section_title),
            style = MaterialTheme.typography.titleLarge
        )
    }
    items(sampleDayDetailsDrinks, key = { it.uid }) { drink ->
        DrinkListItem(drink = drink)
    }
}
```

- [ ] **Step 6: Build the design checkpoint**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 7: Commit design checkpoint**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails app/src/main/res/values/strings.xml
git commit -m "feat: add day details design preview"
```

---

## Task 4: Stage 3 Maestro Design Checkpoint

**Files:**
- Create: `maestro/flows/calendar-day-details.yaml`

- [ ] **Step 1: Add focused Maestro flow for the reviewable screen**

Create `calendar-day-details.yaml`:

```yaml
appId: com.mgruchala.drinkwise.dev
name: Calendar Day Details
tags:
  - navigation
  - day-details
---
- launchApp:
    clearState: true
- extendedWaitUntil:
    visible: "Today"
    timeout: 10000
- tapOn: "Calendar"
- assertVisible: "Mon"
- tapOn:
    text: "Open details for .*"
- assertVisible: "Drinks"
- assertVisible: "units"
- assertNotVisible: "Calendar"
- takeScreenshot: calendar-day-details-design
- tapOn: "Navigate back"
- assertVisible: "Calendar"
```

- [ ] **Step 2: Run the focused Maestro flow**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
```

Expected: flow passes and prints the exact timestamped artifact directory under `artifacts/maestro`.

- [ ] **Step 3: Commit the Maestro checkpoint flow**

Run:

```bash
git add maestro/flows/calendar-day-details.yaml
git commit -m "test: add day details maestro flow"
```

- [ ] **Step 4: Stop for user design review**

Report:

```text
Stage 3 is ready for design review. The Day Details screen is reachable from Calendar and uses mock data. Maestro flow passed and screenshot artifacts are in the timestamped artifacts directory printed by the runner.
```

Do not continue to Task 5 until the user approves the design screen.

---

## Task 5: Date Filtering And Format Tests

**Files:**
- Create: `app/src/main/java/com/mgruchala/drinkwise/utils/time/LocalDateRange.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt`
- Create: `app/src/test/java/com/mgruchala/drinkwise/utils/time/LocalDateRangeTest.kt`
- Create: `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayDetailsFormattersTest.kt`

- [ ] **Step 1: Write date range test**

Create `LocalDateRangeTest.kt`:

```kotlin
package com.mgruchala.drinkwise.utils.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class LocalDateRangeTest {

    @Test
    fun `creates inclusive local day range`() {
        val zoneId = ZoneId.of("Europe/Warsaw")
        val range = LocalDate.of(2026, 5, 16).toEpochMillisRange(zoneId)

        val expectedStart = LocalDate.of(2026, 5, 16)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 5, 17)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli() - 1

        assertEquals(expectedStart, range.startMillis)
        assertEquals(expectedEnd, range.endMillis)
    }
}
```

- [ ] **Step 2: Run date range test and verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.mgruchala.drinkwise.utils.time.LocalDateRangeTest
```

Expected: fails because `toEpochMillisRange` does not exist.

- [ ] **Step 3: Implement date range helper**

Create `LocalDateRange.kt`:

```kotlin
package com.mgruchala.drinkwise.utils.time

import java.time.LocalDate
import java.time.ZoneId

data class EpochMillisRange(
    val startMillis: Long,
    val endMillis: Long
)

fun LocalDate.toEpochMillisRange(zoneId: ZoneId = ZoneId.systemDefault()): EpochMillisRange {
    val startMillis = atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val endMillis = plusDays(1)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli() - 1
    return EpochMillisRange(startMillis, endMillis)
}
```

- [ ] **Step 4: Add repository method**

In `DrinksRepository.kt`, add:

```kotlin
import java.time.LocalDate
```

and:

```kotlin
fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>>
```

In `DrinksRepositoryImpl.kt`, add:

```kotlin
import com.mgruchala.drinkwise.utils.time.toEpochMillisRange
import java.time.LocalDate
```

and:

```kotlin
override fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>> {
    val range = date.toEpochMillisRange()
    return drinkDao.getPaginatedDrinksByDateRange(
        startDate = range.startMillis,
        endDate = range.endMillis
    )
}
```

- [ ] **Step 5: Write formatter tests**

Create `DayDetailsFormattersTest.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DayDetailsFormattersTest {

    @Test
    fun `formats units without decimals for whole values`() {
        assertEquals("4", formatDayDetailsUnits(4f))
    }

    @Test
    fun `formats units with one decimal for fractional values`() {
        assertEquals("4.2", formatDayDetailsUnits(4.24f))
    }

    @Test
    fun `formats milliliters below one liter`() {
        assertEquals("330 ml", formatDayDetailsVolume(330))
    }

    @Test
    fun `formats liters at and above one liter`() {
        assertEquals("1.5 L", formatDayDetailsVolume(1500))
    }

    @Test
    fun `formats abv with percent sign`() {
        assertEquals("5.2%", formatDayDetailsAbv(5.2f))
    }
}
```

- [ ] **Step 6: Run unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.mgruchala.drinkwise.utils.time.LocalDateRangeTest --tests com.mgruchala.drinkwise.presentation.daydetails.components.DayDetailsFormattersTest
```

Expected: both tests pass.

- [ ] **Step 7: Commit data helper work**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/utils/time/LocalDateRange.kt app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt app/src/test/java/com/mgruchala/drinkwise/utils/time/LocalDateRangeTest.kt app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/components/DayDetailsFormattersTest.kt
git commit -m "feat: add day details date filtering"
```

---

## Task 6: Real Day Details State Integration

**Files:**
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsState.kt`
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`

- [ ] **Step 1: Add final state model**

Create `DayDetailsState.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails

import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import java.time.LocalDate

data class DayDetailsState(
    val selectedDate: LocalDate = LocalDate.now(),
    val drinks: List<DrinkEntity> = emptyList(),
    val totalUnits: Float = 0f,
    val dailyLimit: Float = 1f,
    val alcoholUnitLevel: AlcoholUnitLevel = AlcoholUnitLevel.Low(0f, 1f),
    val isLoading: Boolean = true
)
```

- [ ] **Step 2: Add ViewModel**

Create `DayDetailsViewModel.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.navigaiton.AppRoute
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DayDetailsViewModel @Inject constructor(
    drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val selectedDate = LocalDate.ofEpochDay(
        savedStateHandle.get<Long>(AppRoute.DayDetails.ARG_EPOCH_DAY)
            ?: LocalDate.now().toEpochDay()
    )

    val state: StateFlow<DayDetailsState> = combine(
        drinksRepository.getDrinksForDate(selectedDate),
        alcoholLimitPreferencesDataSource.preferences
    ) { drinks, preferences ->
        val totalUnits = drinks.sumOf { drink ->
            calculateAlcoholUnits(drink.quantity, drink.alcoholContent)
        }.toFloat()
        val dailyLimit = preferences.dailyAlcoholUnitLimit.coerceAtLeast(0.1f)
        DayDetailsState(
            selectedDate = selectedDate,
            drinks = drinks,
            totalUnits = totalUnits,
            dailyLimit = dailyLimit,
            alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(totalUnits, dailyLimit),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayDetailsState(selectedDate = selectedDate)
    )
}
```

- [ ] **Step 3: Wire screen to ViewModel and remove mock data**

In `DayDetailsScreen.kt`, change the public composable signature:

```kotlin
@Composable
fun DayDetailsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    DayDetailsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
```

Add imports:

```kotlin
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
```

Move the existing scaffold into:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailsContent(
    state: DayDetailsState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = state.selectedDate.format(formatter))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.day_details_navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DayConsumptionIndicator(alcoholUnitLevel = state.alcoholUnitLevel)
                }
            }
            item {
                Text(
                    text = stringResource(R.string.day_details_drinks_section_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (state.drinks.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.day_details_no_drinks),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.drinks, key = { it.uid }) { drink ->
                    DrinkListItem(drink = drink)
                }
            }
        }
    }
}
```

Delete `sampleDayDetailsDrinks` and `sampleDayDetailsLevel`.

- [ ] **Step 4: Update navigation destination to ViewModel screen**

In `AppNavigation.kt`, change the Day Details destination body to:

```kotlin
DayDetailsScreen(
    onNavigateBack = { navController.popBackStack() }
)
```

Keep the `navArgument` declaration so Hilt/SavedStateHandle receives `epochDay`.

- [ ] **Step 5: Build real integration**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 6: Commit real integration**

Run:

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt
git commit -m "feat: wire day details data"
```

---

## Task 7: Final Verification

**Files:**
- Verify: `maestro/flows/calendar-day-details.yaml`

- [ ] **Step 1: Run full JVM tests**

Run:

```bash
./gradlew test
```

Expected: all unit tests pass.

- [ ] **Step 2: Run debug build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: debug APK builds successfully.

- [ ] **Step 3: Run final Maestro flow**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
```

Expected: Calendar -> Day Details -> back flow passes on the simulator, with screenshot artifacts in the timestamped directory printed by the runner.

- [ ] **Step 4: Inspect artifacts**

Run:

```bash
find artifacts/maestro -maxdepth 3 -type f | sort | tail -20
```

Expected: the latest run includes a `calendar-day-details` screenshot or Maestro report artifacts.

- [ ] **Step 5: Confirm Maestro flow file status**

Run:

```bash
git status --short maestro/flows/calendar-day-details.yaml
```

Expected: no output. If there is output, inspect the diff and commit the intentional selector update with `git add maestro/flows/calendar-day-details.yaml` and `git commit -m "test: stabilize day details maestro flow"`.

- [ ] **Step 6: Final report**

Report:

```text
Implemented Calendar -> Day Details -> back. Unit tests passed, debug build passed, and Maestro calendar-day-details flow passed on the simulator. Maestro artifacts are in the timestamped directory printed by the runner.
```
