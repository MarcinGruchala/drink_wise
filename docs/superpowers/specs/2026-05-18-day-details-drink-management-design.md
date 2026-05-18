# Day Details Drink Management Design

## Goal

Allow users to manage drinks directly from the Day Details screen so they can correct past history after noticing an incorrect log entry.

The feature covers:

- editing an existing drink from a selected day,
- deleting a drink from a selected day,
- undoing a confirmed delete,
- adding one or more drinks to the selected day.

Moving drinks to a different date is out of scope for this version.

## Current Context

Drink Wise is an Android/Kotlin Jetpack Compose app using Material 3, Hilt, Room, DataStore Preferences, Navigation Compose, and `StateFlow`-backed ViewModels.

Relevant existing pieces:

- `DayDetailsScreen` is reachable from `CalendarScreen` via `day_details/{epochDay}`.
- `DayDetailsViewModel` reads the selected date from `SavedStateHandle`, observes drinks for that date, combines them with daily limit preferences, and exposes `DayDetailsState`.
- `DrinkListItem` currently displays drink quantity, ABV, calculated units, and time, but rows are not interactive.
- `HomeScreen` has an add FAB that opens `AddDrinkDialog`, which reuses `AlcoholCalculatorContent`.
- `HomeScreenViewModel.registerNewDrinks` creates one or more `DrinkEntity` rows using `clock.nowMillis()`.
- `DrinkEntity` contains `uid`, `quantity`, `alcoholContent`, and `timestamp`; no drink name/category exists.
- `DrinkDao` already supports range reads, insert with replace strategy, and delete.
- `DrinksRepository` exposes date reads, add, and delete. It does not yet expose an explicit update operation.
- `SettingsScreen` already demonstrates Material snackbar usage.
- `maestro/flows/calendar-day-details.yaml` verifies opening Day Details but does not cover managing drinks.

## User Flow

The Day Details screen remains the history-management surface for one selected local date.

Opening a day:

- The user opens Calendar.
- The user taps a day.
- Day Details opens for that date and shows the existing consumption indicator and drinks list.
- The bottom navigation remains hidden, matching the current Day Details behavior.

Adding a drink:

- A FAB on Day Details opens a dedicated drink editor modal bottom sheet in add mode.
- Add mode defaults the date to the currently selected Day Details date.
- Add mode defaults the time to the current clock time applied to the selected date.
- Add mode supports `numberOfDrinks`, matching the current Home add flow.
- Saving creates one or more separate `DrinkEntity` rows with the selected date/time, quantity, and ABV.
- The sheet closes after save, and the Flow-backed Day Details list, totals, and indicator update automatically.

Editing a drink:

- The user taps an existing drink row.
- A dedicated drink editor modal bottom sheet opens in edit mode.
- Edit mode loads the selected drink's quantity, ABV, and time.
- The date is fixed to the Day Details date in this version.
- Edit mode edits exactly one persisted drink row; it does not show the number-of-drinks stepper.
- `Save` updates the same drink `uid`, closes the sheet, and the Flow-backed list, totals, and indicator update automatically.

Deleting a drink:

- In edit mode, `Delete` does not delete immediately.
- Tapping `Delete` switches the bottom sheet into an inline confirmation state.
- Confirming delete closes the sheet, removes the drink, updates Day Details immediately, and shows a snackbar on Day Details.
- The snackbar says that the drink was deleted and offers `Undo`.
- Tapping `Undo` restores the deleted drink with its original quantity, ABV, and timestamp.

## UI And Interaction Design

Use a dedicated `DrinkEditorSheet` rather than extending the existing `AddDrinkDialog` directly. The sheet can reuse smaller calculator components if they fit cleanly, but the editor owns its add/edit/delete-specific layout and behavior.

Day Details screen:

- Keep the existing top app bar, indicator, and drink list structure.
- Add a Material FAB with an Add icon and a clear content description.
- Add enough list bottom padding so the FAB does not obscure the final row.
- Empty days keep a text-only empty state, such as `No drinks recorded`.
- Drink rows remain visually simple: leading drink icon, generic `Drink` label, details text, and no trailing edit/delete actions.
- The entire drink row is tappable in edit mode.

Drink editor sheet:

- Use a Material 3 modal bottom sheet with a drag handle.
- Follow the Google Clock-inspired interaction direction: a substantial editing surface, with the primary editable time near the top and actions at the bottom.
- Show quantity and ABV as Material text fields.
- Use a proper Material time picker for time editing.
- Show `Save` as the primary commit action.
- In add mode, omit delete and include the number-of-drinks stepper.
- In edit mode, include `Delete` on the opposite side from `Save`, and omit the number-of-drinks stepper.
- Disable `Save` until quantity, ABV, and time are valid.
- Show inline validation for invalid inputs where the UI can express the error clearly.
- Show the selected date as read-only supporting context near the time control.

Delete confirmation:

- Confirmation stays inline in the bottom sheet rather than opening a separate alert dialog.
- The confirmation state clearly asks whether to delete the drink.
- It has a non-destructive cancel action and a destructive confirm action.
- Cancel returns to the normal edit state without closing the sheet.

Accessibility:

- The FAB has a content description such as `Add drink`.
- Drink rows expose button semantics and a label such as `Edit drink, 175 ml, 13.5 percent alcohol, 2.4 units, consumed at 20:15`.
- Decorative icons use `contentDescription = null`.
- Interactive controls meet the 48dp minimum touch target.
- Delete confirmation and snackbar actions are reachable with accessibility services.
- All user-visible strings live in `app/src/main/res/values/strings.xml`.

## Data And Domain Design

No database schema migration is required.

Add an explicit update path:

- Add `DrinksRepository.updateDrink(drink: DrinkEntity)`.
- Add `DrinkDao.updateDrink(drink: DrinkEntity)` with `@Update`.

Timestamp rules:

- The selected Day Details `LocalDate` owns the date portion for add and edit.
- Add mode uses the injected `Clock` to get the current local time and combines it with the selected date.
- Edit mode derives the initial local time from the drink timestamp.
- Saving in either mode composes `selectedDate + selectedTime` into epoch millis using the system default zone, matching the existing local-date range behavior.
- Time values at `00:00` and `23:59` must remain inside the selected date's range.

Undo rules:

- Confirmed delete stores the deleted `DrinkEntity` long enough to offer snackbar undo.
- Undo reinserts the original entity data.
- If Room auto-generation or conflict handling requires a new `uid`, the visible behavior still restores the same quantity, ABV, timestamp, totals, and list row content.

State ownership:

- `DayDetailsViewModel` owns durable operations: add, update, delete, and undo.
- Compose can own ephemeral UI state such as whether the sheet is open, which drink is selected, whether delete confirmation is visible, and whether the time picker is open.
- If the editor draft grows beyond simple field state, introduce a small editor state model local to the Day Details feature.

## Navigation Design

No new Navigation Compose destination is needed.

The existing Day Details route stays:

```kotlin
day_details/{epochDay}
```

The drink editor bottom sheet and time picker are local UI surfaces launched from Day Details. Back behavior should be standard Material behavior:

- back closes the time picker if it is open,
- back closes the bottom sheet if the sheet is open,
- back from Day Details returns to Calendar.

## Edge Cases

- Last drink deleted: the list changes to the text-only empty state, the indicator drops to zero, and snackbar undo remains available.
- Delete canceled: the sheet returns to edit mode and the drink is unchanged.
- Undo after delete: the drink returns with the original quantity, ABV, and timestamp.
- Edit changes totals: row details, total units, and indicator update immediately after save.
- Add on a past day: timestamp uses the selected date plus current clock time unless the user changes time in the picker.
- Add multiple drinks: add mode creates separate rows with the same selected timestamp, quantity, and ABV, matching the current Home add behavior.
- Invalid quantity: save is unavailable until quantity is a valid positive number.
- Invalid ABV: save is unavailable until ABV is in the inclusive range `0..100`.
- Midnight and end-of-day times: saved timestamps remain inside the selected date.
- Date reassignment: not supported in this version.

## Testing Strategy

Use `android-testing` for JVM/unit coverage and `android-maestro-verification` for end-to-end Android UI verification.

JVM tests:

- Add tests for helper logic that composes selected date and local time into epoch millis.
- Add tests for add/edit form validation, especially quantity, ABV, and save enablement if implemented outside pure Compose UI.
- Add `DayDetailsViewModel` tests with fake repositories if the new operations can be isolated without large infrastructure.
- Add repository/DAO tests for explicit update behavior if a new DAO update method is introduced.

Compose previews:

- Day Details with drinks and FAB.
- Day Details empty state with FAB and text-only empty message.
- Drink editor add mode.
- Drink editor edit mode.
- Drink editor delete-confirmation state.
- Drink editor validation-error state.

Maestro:

- Add or extend a focused flow under `maestro/flows/`, likely `calendar-day-details-manage-drinks.yaml`.
- Launch the debug app with clear state.
- Open Calendar and navigate to a Day Details screen.
- Add a drink from Day Details using the FAB.
- Verify the new row and updated total/indicator text are visible.
- Tap the row, edit drink details, save, and verify updated values are visible.
- Delete the drink, confirm inline, verify it disappears and totals update.
- Tap snackbar `Undo` and verify the drink returns.
- Confirm a final delete path if needed to verify the empty-state transition.

Prefer visible text, content descriptions, and stable semantics over coordinate taps.

## Acceptance Criteria

- Day Details includes a FAB for adding drinks to the selected day.
- Add mode opens a dedicated Material modal bottom sheet.
- Add mode defaults to the selected date and current clock time.
- Add mode can create multiple separate drinks.
- Tapping an existing drink row opens the same editor sheet in edit mode.
- Edit mode updates quantity, ABV, and time for exactly one drink.
- Edit mode keeps the drink on the selected Day Details date.
- Delete requires inline confirmation inside the sheet.
- Confirmed delete closes the sheet, updates the Day Details UI, and shows snackbar undo.
- Undo restores the deleted drink.
- Last-drink deletion shows the text-only empty state and a zeroed indicator.
- The existing Calendar to Day Details navigation remains unchanged.
- No database migration is required.
- User-visible strings are in resources.
- Focused JVM tests and a Maestro management flow cover the main behavior.

## Out Of Scope

- Moving a drink to another date.
- Drink names, categories, icons, or presets.
- Full-screen add/edit navigation.
- Swipe-to-delete.
- Alert-dialog delete confirmation.
- Release builds or signing changes.
- Large navigation, data-layer, or design-system refactors.
