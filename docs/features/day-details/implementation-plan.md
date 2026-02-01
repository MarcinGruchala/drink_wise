# Day Details - Implementation Plan

This document breaks down the Day Details feature into small, incremental subplans. Each subplan represents a single, testable unit of work that builds on the previous one.

---

## Subplan 1: Data Layer - Add Date-Filtered Query

**Goal**: Add a method to fetch drinks for a specific date range (single day).

### Files to Modify

1. **`alcohol-database/src/main/java/com/mgruchala/alcohol_database/DrinkDao.kt`**
   - The `getPaginatedDrinksByDateRange` method already exists and can be reused

2. **`app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt`**
   - Add method signature:
   ```kotlin
   fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>>
   ```

3. **`app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt`**
   - Implement the new method using `getPaginatedDrinksByDateRange`
   - Convert LocalDate to start/end timestamps (00:00:00 to 23:59:59)

### Acceptance Criteria
- [ ] `getDrinksForDate(LocalDate)` returns a Flow of drinks for that specific day
- [ ] Method correctly handles timezone conversion
- [ ] Existing tests still pass

---

## Subplan 2: String Resources

**Goal**: Add all string resources needed for the Day Details screen.

### Files to Modify

1. **`app/src/main/res/values/strings.xml`**
   - Add strings:
   ```xml
   <!-- Day Details Screen -->
   <string name="day_details_no_drinks">No drinks recorded</string>
   <string name="day_details_units_label">units</string>
   <string name="day_details_retry">Retry</string>
   <string name="day_details_error_loading">Failed to load drinks</string>
   <string name="day_details_navigate_back">Navigate back</string>
   <string name="day_details_drink_icon">Drink</string>
   <!-- Day Details Accessibility -->
   <string name="day_details_a11y_indicator">%1$s of %2$s units consumed, %3$d percent of daily limit</string>
   <string name="day_details_a11y_over_limit">, exceeding daily limit</string>
   <string name="day_details_a11y_drink_item">%1$s, %2$s, %3$s percent alcohol, %4$s units</string>
   <!-- Relative time -->
   <string name="relative_time_today">Today</string>
   <string name="relative_time_yesterday">Yesterday</string>
   <string name="relative_time_days_ago">%d days ago</string>
   <string name="relative_time_weeks_ago">%d weeks ago</string>
   <string name="relative_time_months_ago">%d months ago</string>
   <string name="relative_time_years_ago">%d years ago</string>
   ```

### Acceptance Criteria
- [ ] All strings added to strings.xml
- [ ] App builds successfully
- [ ] No duplicate resource names

---

## Subplan 3: Navigation Route Setup

**Goal**: Add DayDetails route to navigation and enable navigation from Calendar.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppRoute.kt`**
   - Add new route:
   ```kotlin
   data class DayDetails(val epochDay: Long) : AppRoute("day_details/{epochDay}") {
       companion object {
           const val ROUTE_PATTERN = "day_details/{epochDay}"
           const val ARG_EPOCH_DAY = "epochDay"
       }
   }
   ```

2. **`app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`**
   - Add composable destination for DayDetails route
   - Pass epochDay argument to the screen
   - Create placeholder DayDetailsScreen (just Text showing the date)
   - Note: Bottom nav should be hidden on this screen

3. **`app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`**
   - Add `onDayClick: (LocalDate) -> Unit` parameter
   - Make day cells clickable
   - Wire up navigation in AppNavigation.kt

### Acceptance Criteria
- [ ] Tapping a day in Calendar navigates to DayDetails
- [ ] DayDetails screen receives and displays the correct date
- [ ] Back navigation returns to Calendar
- [ ] Bottom navigation bar is hidden on DayDetails screen

---

## Subplan 4: ViewModel - State Management

**Goal**: Create DayDetailsViewModel with proper state management.

### Files to Create

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsState.kt`**
   ```kotlin
   data class DayDetailsState(
       val selectedDate: LocalDate,
       val drinks: List<DrinkEntity> = emptyList(),
       val totalUnits: Float = 0f,
       val dailyLimit: Float = 7f,
       val alcoholUnitLevel: AlcoholUnitLevel = AlcoholUnitLevel.Low(0f, 7f),
       val isLoading: Boolean = true,
       val error: String? = null
   )
   ```

2. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`**
   - Inject `DrinksRepository` and `AlcoholLimitPreferencesDataSource`
   - Accept `epochDay: Long` as SavedStateHandle argument
   - Combine drinks flow and preferences flow
   - Calculate total units and alcohol level
   - Expose `StateFlow<DayDetailsState>`

### Acceptance Criteria
- [ ] ViewModel correctly loads drinks for the selected date
- [ ] State updates reactively when data changes
- [ ] Total units calculated correctly
- [ ] AlcoholUnitLevel determined based on limit
- [ ] Loading and error states handled

---

## Subplan 5: UI Component - Circular Consumption Indicator

**Goal**: Create a reusable circular consumption indicator with text inside.

### Files to Create

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/ConsumptionCircle.kt`**
   - Composable showing circular progress with text inside
   - Parameters:
     - `consumedUnits: Float`
     - `limitUnits: Float`
     - `alcoholUnitLevel: AlcoholUnitLevel`
     - `modifier: Modifier`
   - Features:
     - Large circle (use Box with Canvas or CircularProgressIndicator)
     - Units text centered inside: "4.2 / 7"
     - "units" label below the numbers
     - Color based on alcoholUnitLevel (reuse existing color logic)
     - Decimal formatting: show only when needed

### Acceptance Criteria
- [ ] Circle displays consumed/limit units
- [ ] Color changes based on consumption level (green/amber/red)
- [ ] Decimal formatting works correctly (4.0 → "4", 4.2 → "4.2")
- [ ] Size is configurable via modifier
- [ ] Preview shows all three consumption levels

---

## Subplan 6: UI Component - Drink List Item

**Goal**: Create a drink list item card component.

### Files to Create

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`**
   - Composable for a single drink entry
   - Parameters:
     - `drink: DrinkEntity`
     - `onClick: () -> Unit`
     - `modifier: Modifier`
   - Layout:
     - Generic drink icon on the left
     - Drink name (use drink type or default name)
     - Volume (context-aware: ml or L) • ABV% • X.X units
   - Ripple effect on tap (no action yet)

2. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/VolumeFormatter.kt`**
   - Utility function to format volume context-aware
   - < 1000ml: "330 ml", "500 ml"
   - >= 1000ml: "1 L", "1.5 L"

### Acceptance Criteria
- [ ] Drink item shows icon, name, volume, ABV, and units
- [ ] Volume formats correctly (ml for small, L for large)
- [ ] Ripple effect visible on tap
- [ ] Preview shows sample drink item

---

## Subplan 7: UI - Basic Screen Layout (Portrait)

**Goal**: Assemble the Day Details screen with basic portrait layout.

### Files to Create/Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - Main screen composable
   - Scaffold with:
     - TopAppBar with back button and date
     - Content: Circle + LazyColumn of drinks
   - Wire up ViewModel
   - Handle loading, error, and content states

2. **`app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`**
   - Update to use actual DayDetailsScreen with ViewModel

### Layout Structure (Portrait)
```
TopAppBar (date + back button)
─────────────────────────────
LazyColumn:
  - ConsumptionCircle (centered)
  - Drinks list with dividers
```

### Acceptance Criteria
- [ ] Screen displays date in top bar
- [ ] Back button navigates to Calendar
- [ ] Circle shows correct consumption data
- [ ] Drink list displays all drinks for the day
- [ ] Dividers between drink items
- [ ] Scrolling works correctly

---

## Subplan 8: UI - Empty, Loading, and Error States

**Goal**: Handle all screen states properly.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - **Loading**: Centered CircularProgressIndicator
   - **Error**: Full-screen error with message and retry button
   - **Empty**: Circle showing 0/limit, centered "No drinks recorded" text
   - **Content**: Normal layout with drinks

### Acceptance Criteria
- [ ] Loading spinner displays while fetching data
- [ ] Error state shows message and retry button
- [ ] Retry button triggers data reload
- [ ] Empty state shows zero consumption and message
- [ ] Transitions between states are smooth

---

## Subplan 9: UI - Large Collapsing App Bar

**Goal**: Implement the large collapsing top app bar with date formatting.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - Replace TopAppBar with LargeTopAppBar
   - Use TopAppBarScrollBehavior for collapse effect
   - Connect to LazyColumn's scroll state

2. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DateFormatter.kt`**
   - Full date format: "Monday, January 27, 2026"
   - Collapsed format: "Mon, Jan 27"
   - Relative time: "Today", "Yesterday", "6 months ago"

### App Bar Content
- **Expanded**: Full date + relative time (secondary text)
- **Collapsed**: Short date only
- **Always visible**: Back arrow

### Acceptance Criteria
- [ ] App bar collapses when scrolling
- [ ] Date format changes when collapsed
- [ ] Relative time shows in expanded state
- [ ] Back button always visible
- [ ] Smooth collapse animation

---

## Subplan 10: Feature - Swipe Navigation Between Days

**Goal**: Enable horizontal swiping to navigate between days.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - Wrap content in HorizontalPager
   - Manage selected date state
   - Block swiping past today (forward limit)
   - Allow unlimited history (backward)

2. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`**
   - Add function to change selected date
   - Or create new ViewModel instance per page

### Implementation Notes
- Use Accompanist Pager or Compose Foundation HorizontalPager
- Handle infinite scrolling backwards
- Show bounce effect at today's date boundary
- Add haptic feedback on day change

### Acceptance Criteria
- [ ] Swipe left goes to next day (toward today)
- [ ] Swipe right goes to previous day
- [ ] Cannot swipe past today (shows resistance)
- [ ] Date in header updates on swipe
- [ ] Data loads for each day
- [ ] Light haptic feedback on transition

---

## Subplan 11: Feature - Circle Fill Animation

**Goal**: Add progressive fill animation to the consumption circle.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/ConsumptionCircle.kt`**
   - Add `animateFloat` for progress value
   - Animate from 0 to current value on composition
   - Use M3 Expressive motion timing
   - Animate on day change (when swiping)

### Acceptance Criteria
- [ ] Circle animates fill on screen load
- [ ] Animation uses smooth easing
- [ ] Animation plays when swiping to new day
- [ ] Duration feels natural (300-500ms)

---

## Subplan 12: Feature - Multiple Rings for Over-Limit

**Goal**: Show proportional multiple rings when consumption exceeds limit.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/ConsumptionCircle.kt`**
   - Calculate number of rings needed: `ceil(consumed / limit)`
   - Draw concentric rings with decreasing size
   - Inner rings fully filled, outer ring proportionally filled
   - All overflow rings use red color

### Visual Logic
- 0-100%: Single ring, partially filled
- 100-200%: Inner ring full, outer ring fills to show overflow
- 200%+: Additional rings as needed

### Acceptance Criteria
- [ ] Single ring for consumption ≤ 100%
- [ ] Two rings for 100-200% consumption
- [ ] Outer ring correctly shows proportional overflow
- [ ] All over-limit rings use red color
- [ ] Animation works for multiple rings

---

## Subplan 13: Feature - Shared Element Transition

**Goal**: Add shared element transition from Calendar day cell to consumption circle.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/navigaiton/AppNavigation.kt`**
   - Set up SharedTransitionLayout
   - Configure shared element between screens

2. **`app/src/main/java/com/mgruchala/drinkwise/presentation/calendar/CalendarScreen.kt`**
   - Add shared element modifier to day cell

3. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - Add shared element modifier to consumption circle

### Implementation Notes
- Use Compose 1.7+ shared element APIs
- Share bounds between day cell and circle
- Fall back to standard transition if not supported

### Acceptance Criteria
- [ ] Day cell morphs into consumption circle on navigation
- [ ] Circle morphs back to day cell on back navigation
- [ ] Transition is smooth and visually appealing
- [ ] Fallback works on older API levels

---

## Subplan 14: Feature - Landscape Layout

**Goal**: Implement adaptive layout for landscape orientation.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - Detect orientation using `LocalConfiguration`
   - Portrait: Circle above list (current layout)
   - Landscape: Circle on left, list on right (Row layout)

### Landscape Layout
```
┌───────────────┬─────────────────────────┐
│               │                         │
│   [Circle]    │   Drink list            │
│               │   (scrollable)          │
│               │                         │
└───────────────┴─────────────────────────┘
```

### Acceptance Criteria
- [ ] Layout adapts to landscape orientation
- [ ] Circle displays on left side
- [ ] List fills right side and scrolls
- [ ] Rotation preserves scroll position
- [ ] App bar works in both orientations

---

## Subplan 15: Accessibility Enhancements

**Goal**: Ensure full accessibility support for TalkBack and other services.

### Files to Modify

1. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/ConsumptionCircle.kt`**
   - Add `semantics` modifier with full announcement
   - Format: "4.2 of 7 units consumed, 60 percent of daily limit"
   - Add ", exceeding daily limit" when over 100%

2. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`**
   - Add `semantics` for drink cards
   - Format: "Craft IPA, 500 milliliters, 6.5 percent alcohol, 3.25 units"

3. **`app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`**
   - Add content descriptions to all interactive elements
   - Ensure proper focus order
   - Announce date when swiping between days

### Acceptance Criteria
- [ ] TalkBack announces circle content correctly
- [ ] TalkBack announces drink items correctly
- [ ] Back button has content description
- [ ] Day change is announced
- [ ] Minimum touch target size (48dp) for all interactive elements

---

## Implementation Order Summary

| # | Subplan | Dependencies | Est. Complexity |
|---|---------|--------------|-----------------|
| 1 | Data Layer | None | Low |
| 2 | String Resources | None | Low |
| 3 | Navigation Setup | None | Medium |
| 4 | ViewModel | 1, 3 | Medium |
| 5 | Consumption Circle | 2 | Medium |
| 6 | Drink List Item | 2 | Low |
| 7 | Basic Screen Layout | 4, 5, 6 | Medium |
| 8 | State Handling | 7 | Low |
| 9 | Collapsing App Bar | 7 | Medium |
| 10 | Swipe Navigation | 7 | High |
| 11 | Fill Animation | 5 | Low |
| 12 | Multiple Rings | 5 | Medium |
| 13 | Shared Element | 7 | High |
| 14 | Landscape Layout | 7 | Medium |
| 15 | Accessibility | 5, 6, 7 | Low |

---

## Testing Checkpoints

After each subplan, verify:

1. **Build**: `./gradlew assembleDebug` succeeds
2. **Tests**: `./gradlew test` passes
3. **Manual**: Feature works as expected on device/emulator
4. **Regression**: Existing features still work

---

## Notes

- Subplans 1-4 establish the foundation and should be done sequentially
- Subplans 5-6 (components) can be done in parallel
- Subplans 10-15 (features) can be reordered based on priority
- Consider adding unit tests for ViewModel and formatting utilities
- Consider adding UI tests for critical user flows
