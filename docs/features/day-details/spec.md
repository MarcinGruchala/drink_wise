# Day Details Feature Specification

## Overview

The Day Details screen allows users to view detailed alcohol consumption information for a specific day, including a visual consumption indicator and a list of all drinks consumed that day.

## Navigation

### Entry Point
- **Trigger**: Single tap on any day cell in the Calendar screen
- **Transition**: Shared element animation where the day cell morphs/expands into the circular consumption indicator
- **Exit**: Back arrow in top-left corner OR system back gesture

### Day Navigation
- **Swipe Navigation**: Horizontal swipe to navigate between days
  - Swipe left → next day (towards today)
  - Swipe right → previous day (into history)
- **Forward Limit**: Cannot swipe past today; shows subtle bounce/resistance at the boundary
- **Backward Limit**: Unlimited history - can navigate back to any date
- **Haptic Feedback**: Light haptic tick when successfully transitioning to a new day
- **Position Indicator**: None - date in header is the only indicator of current position

## Screen Layout

### Portrait Mode

```
┌─────────────────────────────────┐
│ ← Monday, January 27, 2026      │  ← Large collapsing app bar
│   6 months ago                  │    (with relative time)
├─────────────────────────────────┤
│                                 │
│         ┌─────────┐             │
│         │  4.2/7  │             │  ← Circular indicator (60-70% width)
│         │  units  │             │
│         └─────────┘             │
│                                 │
├─────────────────────────────────┤
│ ─────────────────────────────── │  ← Subtle divider
│ 🍺 Craft IPA                    │
│    500 ml • 6.5% • 3.25 units   │
│ ─────────────────────────────── │
│ 🍺 Lager                        │
│    330 ml • 5.0% • 1.65 units   │
│ ─────────────────────────────── │
│ ...                             │
└─────────────────────────────────┘
```

### Landscape Mode

```
┌────────────────────────────────────────────────────────────┐
│ ← Monday, January 27, 2026 • 6 months ago                  │
├─────────────────────────┬──────────────────────────────────┤
│                         │                                  │
│      ┌─────────┐        │  🍺 Craft IPA                    │
│      │  4.2/7  │        │     500 ml • 6.5% • 3.25 units   │
│      │  units  │        │  ─────────────────────────────   │
│      └─────────┘        │  🍺 Lager                        │
│                         │     330 ml • 5.0% • 1.65 units   │
│                         │  ...                             │
└─────────────────────────┴──────────────────────────────────┘
```

## Top App Bar

### Style
- **Type**: Large collapsing top app bar
- **Behavior**: Collapses when scrolling down through the drink list

### Content (Expanded State)
- **Primary Text**: Full date with weekday - "Monday, January 27, 2026"
- **Secondary Text**: Relative time indicator (smaller, below date)
  - Recent: "Today", "Yesterday", "2 days ago", etc.
  - Older: "6 months ago", "1 year ago", etc.
- **Navigation**: Back arrow icon in top-left corner

### Collapsed State
- **Date Format**: Shortened to "Mon, Jan 27" when scrolled
- **Relative Time**: Hidden when collapsed
- **Back Arrow**: Remains visible

## Circular Consumption Indicator

### Size & Position
- **Width**: 60-70% of screen width (portrait)
- **Alignment**: Horizontally centered
- **Scrolling**: Part of scrollable content (scrolls up with the list)

### Content Display
- **Primary Value**: Consumed units / daily limit (e.g., "4.2 / 7")
- **Label**: "units" displayed below the numbers
- **Decimal Formatting**: Show decimals only when not a whole number
  - 4.0 → "4"
  - 4.2 → "4.2"
  - 4.25 → "4.2" (one decimal max)

### Color Scheme
Uses existing `AlcoholUnitLevel` color scheme from the app:

| Level | Condition | Color | Variable |
|-------|-----------|-------|----------|
| Low | ≤70% of limit | Green | `AlcoholUnitLevelLow` (#4CAF50) |
| Alarming | 70-100% of limit | Amber | `AlcoholUnitLevelAlarming` (#FFB74D) |
| High | >100% of limit | Red | `AlcoholUnitLevelHigh` (#E57373) |

**Color Application**: Both the circle fill/ring AND the units text change color based on consumption level.

### Over-Limit Visualization (Multiple Rings)
When consumption exceeds 100% of the daily limit:
- **Proportional Scaling**: Outer ring fills proportionally to show exact overflow
- **Visual Example**:
  - 150% = Inner ring full + outer ring 50% filled
  - 200% = Two full rings
  - 250% = Two full rings + third ring 50% filled
- **Ring Color**: Outer rings use the same red (`AlcoholUnitLevelHigh`) color

### Animation
- **On Screen Load**: Circle progressively fills from 0 to current value
- **Duration**: Smooth animation aligned with M3 Expressive motion scheme
- **Day Transition**: When swiping between days, animate from current fill to new fill

### No Limit Configured
If user hasn't set a daily limit in settings:
- Use default daily limit of 7 units (from `DEFAULT_DAILY_LIMIT`)
- Display functions normally with this default value

## Drink List

### List Structure
- **Container**: LazyColumn for performance with large lists
- **Item Separation**: Subtle dividers between drink items
- **Order**: Reverse chronological (most recent drink first)
- **No timestamps**: Time of consumption is NOT shown

### Drink Card Content

```
┌────────────────────────────────────────┐
│ 🍺  Craft IPA                          │  ← Generic drink icon + Name
│     500 ml • 6.5% • 3.25 units         │  ← Volume • ABV% • Units
└────────────────────────────────────────┘
```

**Fields Displayed:**
1. **Icon**: Generic drink icon (same for all drink types - no category differentiation)
2. **Name**: Drink name (primary text)
3. **Volume**: Context-aware formatting
   - Small volumes: "330 ml", "500 ml"
   - Larger volumes: "0.5 L", "1 L"
4. **ABV**: With percent symbol - "5.0%", "6.5%"
5. **Units**: Prominent display - "1.65 units", "3.25 units"

### Tap Behavior
- **Visual Feedback**: Ripple effect on tap
- **Action**: None (reserved for future Edit feature)
- **Intent**: Indicates interactivity for future editing capability

## Empty State

When a day has no drinks recorded:

```
┌─────────────────────────────────┐
│ ← Monday, January 27, 2026      │
│   6 months ago                  │
├─────────────────────────────────┤
│                                 │
│         ┌─────────┐             │
│         │  0 / 7  │             │
│         │  units  │             │
│         └─────────┘             │
│                                 │
│                                 │
│     No drinks recorded          │  ← Simple centered text
│                                 │
└─────────────────────────────────┘
```

- **Circle**: Shows "0 / [limit] units" with Low (green) color
- **List Area**: Simple centered text "No drinks recorded"
- **No Illustration**: Keep it minimal

## Loading State

- **Indicator**: Circular spinner centered on screen
- **When**: Displayed while fetching data from local database

## Error State

- **Type**: Full screen error
- **Content**: Error message explaining the issue
- **Action**: Retry button to attempt reloading
- **Layout**: Replaces all screen content except back navigation

## Data Behavior

### Auto-Refresh
- Screen automatically updates when underlying data changes
- Supports real-time updates from:
  - Widgets adding drinks
  - Notification quick actions
  - Other app entry points
- Uses Room's Flow-based reactive updates

### Data Source
- Drinks from `DrinkDao` filtered by selected date
- Daily limit from `AlcoholLimitPreferencesDataSource`
- Reactive updates via Kotlin Flow + StateFlow in ViewModel

## Accessibility

### Screen Reader (TalkBack) Support

**Circular Indicator Announcement:**
Full context announcement: "[consumed] of [limit] units consumed, [percentage] percent of daily limit"
- Example: "4.2 of 7 units consumed, 60 percent of daily limit"
- Over limit: "8.4 of 7 units consumed, 120 percent of daily limit, exceeding daily limit"

**Drink Card Announcements:**
- "[Name], [volume], [ABV] percent alcohol, [units] units"
- Example: "Craft IPA, 500 milliliters, 6.5 percent alcohol, 3.25 units"

**Navigation:**
- Back button: "Navigate back"
- Day swipe: Announce new date when transitioning

### Additional Accessibility
- Respect system font scaling
- Support high contrast themes
- Touch targets minimum 48dp

## Orientation Support

- **Portrait**: Primary layout (described above)
- **Landscape**: Adaptive layout with circle on left, list on right
- **Rotation**: Smooth transition, preserves scroll position

## Technical Notes

### Shared Element Transition
- Use Compose shared element transition APIs
- Share the day cell from Calendar → circular indicator
- Fall back to standard navigation if transition fails

### ViewModel State
```kotlin
data class DayDetailsState(
    val selectedDate: LocalDate,
    val drinks: List<Drink>,
    val totalUnits: Float,
    val dailyLimit: Float,
    val alcoholUnitLevel: AlcoholUnitLevel,
    val isLoading: Boolean,
    val error: String?
)
```

### Navigation Arguments
- Pass selected date as navigation argument (LocalDate or epoch days)
- Support deep linking to specific dates

## Future Considerations

This feature is designed to support the upcoming "Edit Day Details" feature:
- Drink cards show ripple feedback anticipating tap-to-edit
- Screen structure accommodates FAB placement for "Add drink" action
- ViewModel pattern supports edit/delete operations

## String Resources

New strings needed in `strings.xml`:
- `day_details_no_drinks` = "No drinks recorded"
- `day_details_units_label` = "units"
- `day_details_retry` = "Retry"
- `day_details_error_loading` = "Failed to load drinks"
- `day_details_a11y_indicator` = "%1$.1f of %2$.1f units consumed, %3$d percent of daily limit"
- `day_details_a11y_over_limit` = ", exceeding daily limit"
- `day_details_navigate_back` = "Navigate back"
