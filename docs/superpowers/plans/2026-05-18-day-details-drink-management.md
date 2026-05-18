# Day Details Drink Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Day Details drink management: add drinks to the selected day, tap rows to edit, delete with inline confirmation, swipe-to-delete, and snackbar undo.

**Architecture:** Keep the current Day Details route and ViewModel boundary. Add an explicit Room/repository update path, a small pure Kotlin editor-state helper layer, and a dedicated Material 3 `DrinkEditorSheet` launched locally from `DayDetailsScreen`. Room/DataStore Flows continue to refresh the drink list, totals, indicator, and calendar data after mutations.

**Tech Stack:** Android Kotlin, Jetpack Compose Material 3, Navigation Compose, Hilt, Room/KSP, DataStore Preferences, JUnit 5, Maestro.

---

## File Structure

- Modify `alcohol-database/src/main/java/com/mgruchala/alcohol_database/DrinkDao.kt`
  - Add explicit `@Update` support for existing drinks.
- Modify `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt`
  - Add `updateDrink(drink: DrinkEntity)`.
- Modify `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt`
  - Delegate update calls to `DrinkDao.updateDrink`.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/editor/DrinkEditorState.kt`
  - Pure state and helpers for add/edit drafts, validation, timestamp composition, and entity creation.
- Create `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/editor/DrinkEditorStateTest.kt`
  - JVM tests for validation, default time, edit loading, timestamp composition, and entity creation.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`
  - Add add, update, delete, swipe-delete, and undo operations.
- Create `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModelMutationTest.kt`
  - JVM tests for ViewModel mutation calls using fakes.
- Create `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkEditorSheet.kt`
  - Dedicated Material 3 bottom sheet for add/edit/delete-confirming states.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`
  - Make rows tappable, accessible, and suitable for swipe wrapping.
- Modify `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`
  - Add FAB, snackbar host, editor sheet state, time picker state, row tap, inline delete, swipe-to-delete, undo snackbar, and previews.
- Modify `app/src/main/res/values/strings.xml`
  - Add all editor, delete, snackbar, accessibility, and validation strings.
- Create `maestro/flows/calendar-day-details-manage-drinks.yaml`
  - End-to-end Day Details management flow.

---

## Task 1: Add Explicit Drink Update API

**Files:**
- Modify: `alcohol-database/src/main/java/com/mgruchala/alcohol_database/DrinkDao.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt`

- [ ] **Step 1: Add Room update import and DAO method**

In `DrinkDao.kt`, add the `Update` import and method:

```kotlin
import androidx.room.Update
```

```kotlin
    @Update
    suspend fun updateDrink(drink: DrinkEntity): Int
```

The full DAO method area should include:

```kotlin
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Update
    suspend fun updateDrink(drink: DrinkEntity): Int

    @Delete
    suspend fun deleteDrink(drink: DrinkEntity): Int
```

- [ ] **Step 2: Add repository update contract**

In `DrinksRepository.kt`, add:

```kotlin
    /**
     * Updates an existing drink in the database.
     */
    suspend fun updateDrink(drink: DrinkEntity): Int
```

Place it between `addDrinks` and `deleteDrink`.

- [ ] **Step 3: Implement repository update**

In `DrinksRepositoryImpl.kt`, add:

```kotlin
    override suspend fun updateDrink(drink: DrinkEntity): Int {
        return drinkDao.updateDrink(drink)
    }
```

Place it between `addDrinks` and `deleteDrink`.

- [ ] **Step 4: Run the narrow compile check**

Run:

```bash
./gradlew :alcohol-database:compileDebugKotlin :app:compileDebugKotlin
```

Expected: both Kotlin compile tasks pass.

- [ ] **Step 5: Commit**

```bash
git add alcohol-database/src/main/java/com/mgruchala/alcohol_database/DrinkDao.kt app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepository.kt app/src/main/java/com/mgruchala/drinkwise/domain/DrinksRepositoryImpl.kt
git commit -m "feat(day-details): add drink update API"
```

---

## Task 2: Add Pure Drink Editor State And Tests

**Files:**
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/editor/DrinkEditorState.kt`
- Create: `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/editor/DrinkEditorStateTest.kt`

- [ ] **Step 1: Write the failing editor-state tests**

Create `DrinkEditorStateTest.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.editor

import com.mgruchala.alcohol_database.DrinkEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DrinkEditorStateTest {

    private val zoneId: ZoneId = ZoneId.of("Europe/Warsaw")
    private val selectedDate: LocalDate = LocalDate.of(2026, 5, 17)

    @Test
    fun `add draft uses current clock time without seconds`() {
        val currentMillis = LocalDateTime.of(2026, 5, 18, 14, 37, 42)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        val draft = DrinkEditorDraft.forAdd(currentTimeMillis = currentMillis, zoneId = zoneId)

        assertEquals(LocalTime.of(14, 37), draft.time)
        assertEquals("1", draft.numberOfDrinksText)
    }

    @Test
    fun `edit draft loads drink values and timestamp time`() {
        val drink = DrinkEntity(
            uid = 42,
            quantity = 175,
            alcoholContent = 13.5f,
            timestamp = LocalDateTime.of(2026, 5, 17, 20, 15)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        )

        val draft = DrinkEditorDraft.forEdit(drink = drink, zoneId = zoneId)

        assertEquals("175", draft.quantityMlText)
        assertEquals("13.5", draft.abvText)
        assertEquals("1", draft.numberOfDrinksText)
        assertEquals(LocalTime.of(20, 15), draft.time)
    }

    @Test
    fun `draft is valid for positive quantity abv in range and positive count`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "500",
            abvText = "5.2",
            numberOfDrinksText = "2",
            time = LocalTime.of(18, 30)
        )

        assertTrue(draft.isValidForAdd)
        assertTrue(draft.isValidForEdit)
    }

    @Test
    fun `draft rejects non-positive quantity`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "0",
            abvText = "5",
            numberOfDrinksText = "1",
            time = LocalTime.of(18, 30)
        )

        assertFalse(draft.isValidForAdd)
        assertFalse(draft.isValidForEdit)
    }

    @Test
    fun `draft rejects abv outside inclusive zero to one hundred range`() {
        val tooHigh = DrinkEditorDraft(
            quantityMlText = "500",
            abvText = "100.1",
            numberOfDrinksText = "1",
            time = LocalTime.of(18, 30)
        )
        val zeroAbv = tooHigh.copy(abvText = "0")

        assertFalse(tooHigh.isValidForAdd)
        assertTrue(zeroAbv.isValidForAdd)
    }

    @Test
    fun `draft rejects non-positive add count`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "500",
            abvText = "5",
            numberOfDrinksText = "0",
            time = LocalTime.of(18, 30)
        )

        assertFalse(draft.isValidForAdd)
        assertTrue(draft.isValidForEdit)
    }

    @Test
    fun `compose timestamp keeps time inside selected day`() {
        val millis = composeDrinkTimestamp(
            selectedDate = selectedDate,
            time = LocalTime.of(23, 59),
            zoneId = zoneId
        )

        val restored = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime()

        assertEquals(LocalDateTime.of(2026, 5, 17, 23, 59), restored)
    }

    @Test
    fun `to add drinks creates separate drink rows with selected timestamp`() {
        val draft = DrinkEditorDraft(
            quantityMlText = "330",
            abvText = "4.8",
            numberOfDrinksText = "3",
            time = LocalTime.of(21, 5)
        )

        val drinks = draft.toAddDrinks(selectedDate = selectedDate, zoneId = zoneId)

        assertEquals(3, drinks.size)
        assertTrue(drinks.all { it.uid == 0 })
        assertTrue(drinks.all { it.quantity == 330 })
        assertTrue(drinks.all { it.alcoholContent == 4.8f })
        assertTrue(drinks.all {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDateTime() ==
                LocalDateTime.of(2026, 5, 17, 21, 5)
        })
    }

    @Test
    fun `to updated drink preserves uid and selected date`() {
        val original = DrinkEntity(
            uid = 7,
            quantity = 500,
            alcoholContent = 5f,
            timestamp = LocalDateTime.of(2026, 5, 17, 18, 30)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        )
        val draft = DrinkEditorDraft(
            quantityMlText = "250",
            abvText = "12.5",
            numberOfDrinksText = "9",
            time = LocalTime.of(22, 10)
        )

        val updated = draft.toUpdatedDrink(
            original = original,
            selectedDate = selectedDate,
            zoneId = zoneId
        )

        assertEquals(7, updated.uid)
        assertEquals(250, updated.quantity)
        assertEquals(12.5f, updated.alcoholContent)
        assertEquals(LocalDateTime.of(2026, 5, 17, 22, 10), Instant.ofEpochMilli(updated.timestamp).atZone(zoneId).toLocalDateTime())
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorStateTest"
```

Expected: FAIL because `DrinkEditorDraft` and helper functions do not exist yet.

- [ ] **Step 3: Implement editor state helpers**

Create `DrinkEditorState.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.editor

import com.mgruchala.alcohol_database.DrinkEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class DrinkEditorDraft(
    val quantityMlText: String = "",
    val abvText: String = "",
    val numberOfDrinksText: String = "1",
    val time: LocalTime
) {
    val quantityMl: Int?
        get() = quantityMlText.toIntOrNull()?.takeIf { it > 0 }

    val abv: Float?
        get() = abvText.toFloatOrNull()?.takeIf { it in 0f..100f }

    val numberOfDrinks: Int?
        get() = numberOfDrinksText.toIntOrNull()?.takeIf { it > 0 }

    val isValidForAdd: Boolean
        get() = quantityMl != null && abv != null && numberOfDrinks != null

    val isValidForEdit: Boolean
        get() = quantityMl != null && abv != null

    fun incrementCount(): DrinkEditorDraft {
        val next = (numberOfDrinks ?: 1) + 1
        return copy(numberOfDrinksText = next.toString())
    }

    fun decrementCount(): DrinkEditorDraft {
        val current = numberOfDrinks ?: 1
        val next = (current - 1).coerceAtLeast(1)
        return copy(numberOfDrinksText = next.toString())
    }

    fun toAddDrinks(
        selectedDate: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<DrinkEntity> {
        require(isValidForAdd) { "Cannot create drinks from invalid add draft." }
        val timestamp = composeDrinkTimestamp(selectedDate = selectedDate, time = time, zoneId = zoneId)
        return List(numberOfDrinks ?: 1) {
            DrinkEntity(
                uid = 0,
                quantity = requireNotNull(quantityMl),
                alcoholContent = requireNotNull(abv),
                timestamp = timestamp
            )
        }
    }

    fun toUpdatedDrink(
        original: DrinkEntity,
        selectedDate: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DrinkEntity {
        require(isValidForEdit) { "Cannot update drink from invalid edit draft." }
        return original.copy(
            quantity = requireNotNull(quantityMl),
            alcoholContent = requireNotNull(abv),
            timestamp = composeDrinkTimestamp(selectedDate = selectedDate, time = time, zoneId = zoneId)
        )
    }

    companion object {
        fun forAdd(
            currentTimeMillis: Long,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): DrinkEditorDraft {
            val currentTime = Instant.ofEpochMilli(currentTimeMillis)
                .atZone(zoneId)
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
            return DrinkEditorDraft(time = currentTime)
        }

        fun forEdit(
            drink: DrinkEntity,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): DrinkEditorDraft {
            val localTime = Instant.ofEpochMilli(drink.timestamp)
                .atZone(zoneId)
                .toLocalTime()
                .withSecond(0)
                .withNano(0)
            return DrinkEditorDraft(
                quantityMlText = drink.quantity.toString(),
                abvText = drink.alcoholContent.toString(),
                numberOfDrinksText = "1",
                time = localTime
            )
        }
    }
}

fun composeDrinkTimestamp(
    selectedDate: LocalDate,
    time: LocalTime,
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return LocalDateTime.of(selectedDate, time.withSecond(0).withNano(0))
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}
```

- [ ] **Step 4: Run editor-state tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorStateTest"
```

Expected: PASS.

- [ ] **Step 5: Run all app unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/editor/DrinkEditorState.kt app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/editor/DrinkEditorStateTest.kt
git commit -m "test(day-details): add drink editor state"
```

---

## Task 3: Add ViewModel Mutation Operations

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`
- Create: `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModelMutationTest.kt`

- [ ] **Step 1: Write ViewModel mutation tests with fakes**

Create `DayDetailsViewModelMutationTest.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails

import androidx.lifecycle.SavedStateHandle
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.navigaiton.AppRoute
import com.mgruchala.drinkwise.presentation.daydetails.editor.composeDrinkTimestamp
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferences
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DayDetailsViewModelMutationTest {

    private val selectedDate = LocalDate.of(2026, 5, 17)
    private val zoneId = ZoneId.systemDefault()
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `add drinks writes rows assigned to selected date`() = runTest(testDispatcher) {
        val repository = FakeDrinksRepository()
        val viewModel = createViewModel(repository)

        viewModel.addDrinks(quantityMl = 330, abv = 4.8f, numberOfDrinks = 2, time = LocalTime.of(21, 5))
        testScheduler.advanceUntilIdle()

        assertEquals(2, repository.addedDrinks.size)
        assertEquals(330, repository.addedDrinks.first().quantity)
        assertEquals(4.8f, repository.addedDrinks.first().alcoholContent)
        assertEquals(
            composeDrinkTimestamp(selectedDate = selectedDate, time = LocalTime.of(21, 5), zoneId = zoneId),
            repository.addedDrinks.first().timestamp
        )
    }

    @Test
    fun `update drink preserves uid and writes selected date timestamp`() = runTest(testDispatcher) {
        val repository = FakeDrinksRepository()
        val viewModel = createViewModel(repository)
        val original = DrinkEntity(uid = 9, quantity = 500, alcoholContent = 5f, timestamp = 1L)

        viewModel.updateDrink(original = original, quantityMl = 250, abv = 12.5f, time = LocalTime.of(22, 10))
        testScheduler.advanceUntilIdle()

        assertEquals(9, repository.updatedDrink?.uid)
        assertEquals(250, repository.updatedDrink?.quantity)
        assertEquals(12.5f, repository.updatedDrink?.alcoholContent)
        assertEquals(
            composeDrinkTimestamp(selectedDate = selectedDate, time = LocalTime.of(22, 10), zoneId = zoneId),
            repository.updatedDrink?.timestamp
        )
    }

    @Test
    fun `delete stores pending drink and undo restores it`() = runTest(testDispatcher) {
        val repository = FakeDrinksRepository()
        val viewModel = createViewModel(repository)
        val drink = DrinkEntity(uid = 3, quantity = 175, alcoholContent = 13.5f, timestamp = 123L)

        viewModel.deleteDrink(drink)
        testScheduler.advanceUntilIdle()

        assertEquals(drink, repository.deletedDrink)

        viewModel.undoLastDeletedDrink()
        testScheduler.advanceUntilIdle()

        assertEquals(drink, repository.addedDrinks.single())

        viewModel.undoLastDeletedDrink()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.addedDrinks.size)
    }

    @BeforeEach
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: FakeDrinksRepository): DayDetailsViewModel {
        return DayDetailsViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(AppRoute.DayDetails.ARG_EPOCH_DAY to selectedDate.toEpochDay())
            ),
            drinksRepository = repository,
            alcoholLimitPreferencesDataSource = FakeAlcoholLimitPreferencesDataSource()
        )
    }

    private class FakeDrinksRepository : DrinksRepository {
        private val drinks = MutableStateFlow<List<DrinkEntity>>(emptyList())
        val addedDrinks = mutableListOf<DrinkEntity>()
        var updatedDrink: DrinkEntity? = null
        var deletedDrink: DrinkEntity? = null

        override fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>> = drinks
        override fun getAllDrinks(): Flow<List<DrinkEntity>> = drinks
        override fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>> = drinks

        override suspend fun addDrinks(vararg drinks: DrinkEntity) {
            addedDrinks += drinks
        }

        override suspend fun updateDrink(drink: DrinkEntity): Int {
            updatedDrink = drink
            return 1
        }

        override suspend fun deleteDrink(drink: DrinkEntity): Int {
            deletedDrink = drink
            return 1
        }
    }

    private class FakeAlcoholLimitPreferencesDataSource : AlcoholLimitPreferencesDataSource {
        override val preferences: Flow<AlcoholLimitPreferences> =
            MutableStateFlow(
                AlcoholLimitPreferences(
                    dailyAlcoholUnitLimit = 4f,
                    weeklyAlcoholUnitLimit = 14f,
                    monthlyAlcoholUnitLimit = 30f
                )
            )

        override suspend fun updateDailyAlcoholLimit(limit: Float) = Unit
        override suspend fun updateWeeklyAlcoholLimit(limit: Float) = Unit
        override suspend fun updateMonthlyAlcoholLimit(limit: Float) = Unit
    }
}
```

- [ ] **Step 2: Add coroutine test dependency if the test does not compile**

If the test compile fails because `kotlinx.coroutines.test` is missing, add this to `gradle/libs.versions.toml`:

```toml
kotlinxCoroutines = "1.9.0"
```

Under `[libraries]`, add:

```toml
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
```

Then add this to `app/build.gradle.kts` test dependencies:

```kotlin
testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 3: Run tests to verify they fail for missing ViewModel methods**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.daydetails.DayDetailsViewModelMutationTest"
```

Expected: FAIL because `addDrinks`, `updateDrink`, `deleteDrink`, and `undoLastDeletedDrink` are not implemented on `DayDetailsViewModel`.

- [ ] **Step 4: Implement mutation methods in DayDetailsViewModel**

Update constructor property for repository:

```kotlin
class DayDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource
) : ViewModel() {
```

Add imports:

```kotlin
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.presentation.daydetails.editor.composeDrinkTimestamp
import kotlinx.coroutines.launch
import java.time.LocalTime
```

Add property near `selectedDate`:

```kotlin
    private var lastDeletedDrink: DrinkEntity? = null
```

Add methods before the closing brace:

```kotlin
    fun addDrinks(
        quantityMl: Int,
        abv: Float,
        numberOfDrinks: Int,
        time: LocalTime
    ) {
        viewModelScope.launch {
            val timestamp = composeDrinkTimestamp(selectedDate = selectedDate, time = time)
            val drinks = List(numberOfDrinks.coerceAtLeast(1)) {
                DrinkEntity(
                    uid = 0,
                    quantity = quantityMl,
                    alcoholContent = abv,
                    timestamp = timestamp
                )
            }
            drinksRepository.addDrinks(*drinks.toTypedArray())
        }
    }

    fun updateDrink(
        original: DrinkEntity,
        quantityMl: Int,
        abv: Float,
        time: LocalTime
    ) {
        viewModelScope.launch {
            val timestamp = composeDrinkTimestamp(selectedDate = selectedDate, time = time)
            drinksRepository.updateDrink(
                original.copy(
                    quantity = quantityMl,
                    alcoholContent = abv,
                    timestamp = timestamp
                )
            )
        }
    }

    fun deleteDrink(drink: DrinkEntity) {
        viewModelScope.launch {
            lastDeletedDrink = drink
            drinksRepository.deleteDrink(drink)
        }
    }

    fun undoLastDeletedDrink() {
        viewModelScope.launch {
            val drink = lastDeletedDrink ?: return@launch
            drinksRepository.addDrinks(drink)
            lastDeletedDrink = null
        }
    }
```

- [ ] **Step 5: Run mutation tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.daydetails.DayDetailsViewModelMutationTest"
```

Expected: PASS.

- [ ] **Step 6: Run all app unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModelMutationTest.kt app/build.gradle.kts gradle/libs.versions.toml
git commit -m "feat(day-details): add drink mutation actions"
```

If `app/build.gradle.kts` and `gradle/libs.versions.toml` were not changed because the coroutine test dependency already existed, leave them out of `git add`.

---

## Task 4: Add Strings For Drink Management UI

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add strings**

Add these strings under the existing Day Details section:

```xml
    <string name="day_details_add_drink">Add drink</string>
    <string name="day_details_edit_drink">Edit drink</string>
    <string name="day_details_save_drink">Save</string>
    <string name="day_details_delete_drink">Delete</string>
    <string name="day_details_cancel_delete">Cancel</string>
    <string name="day_details_confirm_delete">Delete</string>
    <string name="day_details_delete_confirmation_title">Delete this drink?</string>
    <string name="day_details_delete_confirmation_message">This removes it from this day.</string>
    <string name="day_details_drink_deleted">Drink deleted</string>
    <string name="day_details_undo_delete">Undo</string>
    <string name="day_details_quantity_label">Quantity (ml)</string>
    <string name="day_details_abv_label">Alcohol content (%)</string>
    <string name="day_details_number_of_drinks">Number of drinks: %1$d</string>
    <string name="day_details_decrease_number_content_description">Decrease number of drinks</string>
    <string name="day_details_increase_number_content_description">Increase number of drinks</string>
    <string name="day_details_time_label">Time</string>
    <string name="day_details_change_time">Change time</string>
    <string name="day_details_selected_date">Selected date: %1$s</string>
    <string name="day_details_invalid_quantity">Enter a quantity greater than 0</string>
    <string name="day_details_invalid_abv">Enter an alcohol percentage from 0 to 100</string>
    <string name="day_details_edit_drink_item_description">Edit drink, %1$s, %2$s alcohol, %3$s units, consumed at %4$s</string>
    <string name="day_details_swipe_delete_background">Delete drink</string>
    <string name="day_details_time_picker_confirm">OK</string>
    <string name="day_details_time_picker_cancel">Cancel</string>
```

- [ ] **Step 2: Run resource compile check**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat(day-details): add drink editor strings"
```

---

## Task 5: Build DrinkEditorSheet Component

**Files:**
- Create: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkEditorSheet.kt`

- [ ] **Step 1: Create sheet component**

Create `DrinkEditorSheet.kt`:

```kotlin
package com.mgruchala.drinkwise.presentation.daydetails.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorDraft
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

enum class DrinkEditorSheetMode {
    Add,
    Edit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkEditorSheet(
    mode: DrinkEditorSheetMode,
    selectedDate: LocalDate,
    draft: DrinkEditorDraft,
    isDeleteConfirming: Boolean,
    onDraftChange: (DrinkEditorDraft) -> Unit,
    onSave: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        DrinkEditorSheetContent(
            mode = mode,
            selectedDate = selectedDate,
            draft = draft,
            isDeleteConfirming = isDeleteConfirming,
            onDraftChange = onDraftChange,
            onSave = onSave,
            onDeleteClick = onDeleteClick,
            onCancelDelete = onCancelDelete,
            onConfirmDelete = onConfirmDelete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun DrinkEditorSheetContent(
    mode: DrinkEditorSheetMode,
    selectedDate: LocalDate,
    draft: DrinkEditorDraft,
    isDeleteConfirming: Boolean,
    onDraftChange: (DrinkEditorDraft) -> Unit,
    onSave: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val canSave = when (mode) {
        DrinkEditorSheetMode.Add -> draft.isValidForAdd
        DrinkEditorSheetMode.Edit -> draft.isValidForEdit
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(
                id = when (mode) {
                    DrinkEditorSheetMode.Add -> R.string.day_details_add_drink
                    DrinkEditorSheetMode.Edit -> R.string.day_details_edit_drink
                }
            ),
            style = MaterialTheme.typography.titleLarge
        )

        TimePickerField(
            time = draft.time,
            onTimeChange = { onDraftChange(draft.copy(time = it)) }
        )

        Text(
            text = stringResource(
                id = R.string.day_details_selected_date,
                selectedDate.format(dateFormatter)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DrinkEditorTextFields(
            draft = draft,
            onDraftChange = onDraftChange
        )

        if (mode == DrinkEditorSheetMode.Add) {
            NumberOfDrinksStepper(
                draft = draft,
                onDraftChange = onDraftChange
            )
        }

        if (mode == DrinkEditorSheetMode.Edit && isDeleteConfirming) {
            DeleteConfirmation(
                onCancelDelete = onCancelDelete,
                onConfirmDelete = onConfirmDelete
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mode == DrinkEditorSheetMode.Edit && !isDeleteConfirming) {
                TextButton(onClick = onDeleteClick) {
                    Text(stringResource(R.string.day_details_delete_drink))
                }
            } else {
                Spacer(modifier = Modifier)
            }

            Button(
                onClick = onSave,
                enabled = canSave
            ) {
                Text(stringResource(R.string.day_details_save_drink))
            }
        }
    }
}

@Composable
private fun DrinkEditorTextFields(
    draft: DrinkEditorDraft,
    onDraftChange: (DrinkEditorDraft) -> Unit
) {
    val quantityError = draft.quantityMlText.isNotBlank() && draft.quantityMl == null
    val abvError = draft.abvText.isNotBlank() && draft.abv == null

    OutlinedTextField(
        value = draft.quantityMlText,
        onValueChange = { onDraftChange(draft.copy(quantityMlText = it)) },
        label = { Text(stringResource(R.string.day_details_quantity_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = quantityError,
        supportingText = {
            if (quantityError) {
                Text(stringResource(R.string.day_details_invalid_quantity))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = draft.abvText,
        onValueChange = { onDraftChange(draft.copy(abvText = it)) },
        label = { Text(stringResource(R.string.day_details_abv_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = abvError,
        supportingText = {
            if (abvError) {
                Text(stringResource(R.string.day_details_invalid_abv))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberOfDrinksStepper(
    draft: DrinkEditorDraft,
    onDraftChange: (DrinkEditorDraft) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                id = R.string.day_details_number_of_drinks,
                draft.numberOfDrinks ?: 1
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onDraftChange(draft.decrementCount()) }) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = stringResource(R.string.day_details_decrease_number_content_description)
            )
        }
        IconButton(onClick = { onDraftChange(draft.incrementCount()) }) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.day_details_increase_number_content_description)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val showPicker = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Button(onClick = { showPicker.value = true }) {
        Text(
            text = stringResource(R.string.day_details_time_label) + ": " + time.format(formatter)
        )
    }

    if (showPicker.value) {
        val pickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                        showPicker.value = false
                    }
                ) {
                    Text(stringResource(R.string.day_details_time_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker.value = false }) {
                    Text(stringResource(R.string.day_details_time_picker_cancel))
                }
            },
            text = {
                TimePicker(state = pickerState)
            }
        )
    }
}

@Composable
private fun DeleteConfirmation(
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.day_details_delete_confirmation_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(R.string.day_details_delete_confirmation_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancelDelete) {
                Text(stringResource(R.string.day_details_cancel_delete))
            }
            TextButton(onClick = onConfirmDelete) {
                Text(stringResource(R.string.day_details_confirm_delete))
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkEditorSheetAddPreview() {
    DrinkWiseTheme {
        DrinkEditorSheetContent(
            mode = DrinkEditorSheetMode.Add,
            selectedDate = LocalDate.of(2026, 5, 17),
            draft = DrinkEditorDraft(
                quantityMlText = "500",
                abvText = "5.2",
                numberOfDrinksText = "2",
                time = LocalTime.of(18, 30)
            ),
            isDeleteConfirming = false,
            onDraftChange = {},
            onSave = {},
            onDeleteClick = {},
            onCancelDelete = {},
            onConfirmDelete = {}
        )
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DrinkEditorSheetEditDeletePreview() {
    DrinkWiseTheme {
        DrinkEditorSheetContent(
            mode = DrinkEditorSheetMode.Edit,
            selectedDate = LocalDate.of(2026, 5, 17),
            draft = DrinkEditorDraft(
                quantityMlText = "175",
                abvText = "13.5",
                numberOfDrinksText = "1",
                time = LocalTime.of(20, 15)
            ),
            isDeleteConfirming = true,
            onDraftChange = {},
            onSave = {},
            onDeleteClick = {},
            onCancelDelete = {},
            onConfirmDelete = {}
        )
    }
}
```

- [ ] **Step 2: Compile the new component**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

If `TimePicker`, `rememberTimePickerState`, or `SwipeToDismissBox` API names differ in this Compose BOM, inspect the current Material 3 API through IDE completion or Gradle compile errors and adjust names in the smallest possible way while preserving the behavior.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkEditorSheet.kt
git commit -m "feat(day-details): add drink editor sheet"
```

---

## Task 6: Make Drink Rows Clickable And Accessible

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt`

- [ ] **Step 1: Update DrinkListItem signature and semantics**

Change the signature:

```kotlin
fun DrinkListItem(
    drink: DrinkEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

Add imports:

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
```

Replace the description string with:

```kotlin
    val description = stringResource(
        id = R.string.day_details_edit_drink_item_description,
        volume,
        abv,
        units,
        time
    )
```

Replace the outer `Column` modifier with:

```kotlin
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }
```

Update previews to pass `onClick = {}`:

```kotlin
            DrinkListItem(
                drink = previewDrink,
                onClick = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
```

- [ ] **Step 2: Compile**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/components/DrinkListItem.kt
git commit -m "feat(day-details): make drink rows editable"
```

---

## Task 7: Wire Day Details FAB, Sheet, Snackbar, And Swipe Delete

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`

- [ ] **Step 1: Add editor UI state**

In `DayDetailsScreen.kt`, add imports:

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkEditorSheet
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkEditorSheetMode
import com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorDraft
import kotlinx.coroutines.launch
```

Add state holder near previews or above `DayDetailsContent`:

```kotlin
private sealed interface DrinkEditorUiState {
    data class Add(val draft: DrinkEditorDraft) : DrinkEditorUiState
    data class Edit(
        val drink: DrinkEntity,
        val draft: DrinkEditorDraft,
        val isDeleteConfirming: Boolean = false
    ) : DrinkEditorUiState
}
```

- [ ] **Step 2: Thread ViewModel actions into DayDetailsContent**

In `DayDetailsScreen`, create a helper callback block:

```kotlin
    DayDetailsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onAddDrinks = viewModel::addDrinks,
        onUpdateDrink = viewModel::updateDrink,
        onDeleteDrink = viewModel::deleteDrink,
        onUndoDelete = viewModel::undoLastDeletedDrink,
        modifier = modifier
    )
```

Update `DayDetailsContent` signature:

```kotlin
private fun DayDetailsContent(
    state: DayDetailsState,
    onNavigateBack: () -> Unit,
    onAddDrinks: (quantityMl: Int, abv: Float, numberOfDrinks: Int, time: java.time.LocalTime) -> Unit,
    onUpdateDrink: (original: DrinkEntity, quantityMl: Int, abv: Float, time: java.time.LocalTime) -> Unit,
    onDeleteDrink: (DrinkEntity) -> Unit,
    onUndoDelete: () -> Unit,
    modifier: Modifier = Modifier
)
```

Update every preview call with no-op lambdas:

```kotlin
            onAddDrinks = { _, _, _, _ -> },
            onUpdateDrink = { _, _, _, _ -> },
            onDeleteDrink = {},
            onUndoDelete = {}
```

- [ ] **Step 3: Add snackbar and editor state inside DayDetailsContent**

At the top of `DayDetailsContent`, after the formatter:

```kotlin
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var editorState by remember { mutableStateOf<DrinkEditorUiState?>(null) }
    val drinkDeletedMessage = stringResource(R.string.day_details_drink_deleted)
    val undoMessage = stringResource(R.string.day_details_undo_delete)

    fun deleteWithUndo(drink: DrinkEntity) {
        onDeleteDrink(drink)
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = drinkDeletedMessage,
                actionLabel = undoMessage,
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete()
            }
        }
    }
```

- [ ] **Step 4: Add FAB and snackbar host to Scaffold**

Update `Scaffold`:

```kotlin
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editorState = DrinkEditorUiState.Add(
                        draft = DrinkEditorDraft.forAdd(currentTimeMillis = System.currentTimeMillis())
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.day_details_add_drink)
                )
            }
        },
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
```

Note: this first UI pass uses `System.currentTimeMillis()` only for the local draft default. Durable saved timestamps still go through `DayDetailsViewModel`; if strict clock injection for the initial draft is required, add a ViewModel method that returns `DrinkEditorDraft.forAdd(clock.nowMillis())` instead of reading system time in Compose.

- [ ] **Step 5: Increase LazyColumn bottom padding**

Change content padding:

```kotlin
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 96.dp),
```

- [ ] **Step 6: Wrap drink rows in SwipeToDismissBox**

Replace the drink `items` block with:

```kotlin
                items(state.drinks, key = { it.uid }) { drink ->
                    SwipeToDeleteDrinkItem(
                        drink = drink,
                        onClick = {
                            editorState = DrinkEditorUiState.Edit(
                                drink = drink,
                                draft = DrinkEditorDraft.forEdit(drink)
                            )
                        },
                        onDelete = { deleteWithUndo(drink) }
                    )
                }
```

Add composable below `DayDetailsContent`:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteDrinkItem(
    drink: DrinkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deleteDescription = stringResource(R.string.day_details_swipe_delete_background)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .semantics { contentDescription = deleteDescription }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier
    ) {
        DrinkListItem(
            drink = drink,
            onClick = onClick
        )
    }
}
```

- [ ] **Step 7: Render the editor sheet**

Add this before the `Scaffold` or after it inside `DayDetailsContent`:

```kotlin
    editorState?.let { currentEditorState ->
        when (currentEditorState) {
            is DrinkEditorUiState.Add -> {
                DrinkEditorSheet(
                    mode = DrinkEditorSheetMode.Add,
                    selectedDate = state.selectedDate,
                    draft = currentEditorState.draft,
                    isDeleteConfirming = false,
                    onDraftChange = { editorState = currentEditorState.copy(draft = it) },
                    onSave = {
                        val draft = currentEditorState.draft
                        if (draft.isValidForAdd) {
                            onAddDrinks(
                                requireNotNull(draft.quantityMl),
                                requireNotNull(draft.abv),
                                requireNotNull(draft.numberOfDrinks),
                                draft.time
                            )
                            editorState = null
                        }
                    },
                    onDeleteClick = {},
                    onCancelDelete = {},
                    onConfirmDelete = {},
                    onDismiss = { editorState = null }
                )
            }

            is DrinkEditorUiState.Edit -> {
                DrinkEditorSheet(
                    mode = DrinkEditorSheetMode.Edit,
                    selectedDate = state.selectedDate,
                    draft = currentEditorState.draft,
                    isDeleteConfirming = currentEditorState.isDeleteConfirming,
                    onDraftChange = { editorState = currentEditorState.copy(draft = it) },
                    onSave = {
                        val draft = currentEditorState.draft
                        if (draft.isValidForEdit) {
                            onUpdateDrink(
                                currentEditorState.drink,
                                requireNotNull(draft.quantityMl),
                                requireNotNull(draft.abv),
                                draft.time
                            )
                            editorState = null
                        }
                    },
                    onDeleteClick = {
                        editorState = currentEditorState.copy(isDeleteConfirming = true)
                    },
                    onCancelDelete = {
                        editorState = currentEditorState.copy(isDeleteConfirming = false)
                    },
                    onConfirmDelete = {
                        editorState = null
                        deleteWithUndo(currentEditorState.drink)
                    },
                    onDismiss = { editorState = null }
                )
            }
        }
    }
```

- [ ] **Step 8: Compile and fix API drift**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

If Material 3 swipe API names differ, adapt only the swipe wrapper. Preserve these behaviors:

- only end-to-start swipe deletes,
- row tap still opens edit sheet,
- delete background is error-colored,
- swipe delete calls the same `deleteWithUndo`.

- [ ] **Step 9: Run app unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt
git commit -m "feat(day-details): wire drink management UI"
```

---

## Task 8: Align Add Draft Default Time With Injected Clock

**Files:**
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt`
- Modify: `app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt`
- Modify: `app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModelMutationTest.kt`

- [ ] **Step 1: Inject Clock into DayDetailsViewModel**

In `DayDetailsViewModel.kt`, add import:

```kotlin
import com.mgruchala.drinkwise.utils.time.Clock
```

Update constructor:

```kotlin
class DayDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource,
    private val clock: Clock
) : ViewModel() {
```

Add import:

```kotlin
import com.mgruchala.drinkwise.presentation.daydetails.editor.DrinkEditorDraft
```

Add method:

```kotlin
    fun createAddDraft(): DrinkEditorDraft {
        return DrinkEditorDraft.forAdd(currentTimeMillis = clock.nowMillis())
    }
```

- [ ] **Step 2: Update DayDetailsScreen to use ViewModel draft**

Thread a new callback:

```kotlin
        createAddDraft = viewModel::createAddDraft,
```

Update `DayDetailsContent` signature:

```kotlin
    createAddDraft: () -> DrinkEditorDraft,
```

Replace FAB draft creation:

```kotlin
                    editorState = DrinkEditorUiState.Add(
                        draft = createAddDraft()
                    )
```

Update previews with:

```kotlin
            createAddDraft = {
                DrinkEditorDraft(
                    quantityMlText = "",
                    abvText = "",
                    numberOfDrinksText = "1",
                    time = java.time.LocalTime.of(12, 0)
                )
            },
```

- [ ] **Step 3: Update ViewModel test fake clock**

In `DayDetailsViewModelMutationTest.kt`, add imports:

```kotlin
import com.mgruchala.drinkwise.utils.time.Clock
```

Pass a fake clock in `createViewModel`:

```kotlin
            clock = FakeClock(
                nowMillis = java.time.LocalDateTime.of(2026, 5, 18, 14, 37)
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli()
            )
```

Add fake class:

```kotlin
    private class FakeClock(private val nowMillis: Long) : Clock {
        override fun nowMillis(): Long = nowMillis
    }
```

Add test:

```kotlin
    @Test
    fun `create add draft uses injected clock`() {
        val repository = FakeDrinksRepository()
        val viewModel = createViewModel(repository)

        val draft = viewModel.createAddDraft()

        assertEquals(LocalTime.of(14, 37), draft.time)
    }
```

- [ ] **Step 4: Run tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mgruchala.drinkwise.presentation.daydetails.DayDetailsViewModelMutationTest"
```

Expected: PASS.

- [ ] **Step 5: Compile**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModel.kt app/src/main/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsScreen.kt app/src/test/java/com/mgruchala/drinkwise/presentation/daydetails/DayDetailsViewModelMutationTest.kt
git commit -m "refactor(day-details): use clock for add draft"
```

---

## Task 9: Add Maestro Drink Management Flow

**Files:**
- Create: `maestro/flows/calendar-day-details-manage-drinks.yaml`

- [ ] **Step 1: Create the Maestro flow**

Create `calendar-day-details-manage-drinks.yaml`:

```yaml
appId: com.mgruchala.drinkwise.dev
name: Calendar Day Details Manage Drinks
tags:
  - navigation
  - day-details
  - drink-management
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
- tapOn: "Add drink"
- assertVisible: "Add drink"
- tapOn: "Quantity \\(ml\\)"
- inputText: "250"
- tapOn: "Alcohol content \\(\\%\\)"
- inputText: "12"
- tapOn: "Save"
- assertVisible: "Drink"
- assertVisible: "250 ml .* 12.0% .*"
- tapOn:
    text: "Edit drink.*250 ml.*"
- assertVisible: "Edit drink"
- tapOn: "Quantity \\(ml\\)"
- eraseText
- inputText: "330"
- tapOn: "Save"
- assertVisible: "330 ml .* 12.0% .*"
- tapOn:
    text: "Edit drink.*330 ml.*"
- tapOn: "Delete"
- assertVisible: "Delete this drink?"
- tapOn:
    text: "Delete"
    index: 1
- assertVisible: "Drink deleted"
- tapOn: "Undo"
- assertVisible: "330 ml .* 12.0% .*"
- swipe:
    from:
      text: "Edit drink.*330 ml.*"
    direction: LEFT
- assertVisible: "Drink deleted"
- tapOn: "Undo"
- assertVisible: "330 ml .* 12.0% .*"
- takeScreenshot: calendar-day-details-manage-drinks
```

- [ ] **Step 2: Run the existing Day Details navigation flow**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
```

Expected: PASS.

- [ ] **Step 3: Run the new management flow**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details-manage-drinks.yaml
```

Expected: PASS.

If selectors need adjustment after inspecting the actual semantics, prefer changing production content descriptions or visible resource strings over coordinate taps.

- [ ] **Step 4: Inspect screenshot artifacts**

Check the artifacts directory printed by the runner. Confirm the final screenshot shows Day Details with the managed drink row after undo.

- [ ] **Step 5: Commit**

```bash
git add maestro/flows/calendar-day-details-manage-drinks.yaml
git commit -m "test(day-details): add drink management maestro flow"
```

---

## Task 10: Final Verification

**Files:**
- No code changes expected.

- [ ] **Step 1: Run unit tests**

Run:

```bash
./gradlew test
```

Expected: PASS.

- [ ] **Step 2: Run debug build**

Run:

```bash
./gradlew assembleDebug
```

Expected: PASS. Do not run release builds for this feature because release is minified and depends on local signing configuration.

- [ ] **Step 3: Run Maestro Day Details flows**

Run:

```bash
scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
scripts/android-maestro-run.sh maestro/flows/calendar-day-details-manage-drinks.yaml
```

Expected: both flows PASS.

- [ ] **Step 4: Inspect git status**

Run:

```bash
git status --short
```

Expected: clean worktree.

- [ ] **Step 5: If needed, commit final polish**

If final verification required selector, string, or compile-polish edits, commit them:

```bash
git add app/src/main/java/com/mgruchala/drinkwise app/src/main/res/values/strings.xml maestro/flows
git commit -m "fix(day-details): polish drink management flow"
```

If no final polish edits were needed, do not create an empty commit.

---

## Self-Review Notes

- Spec coverage: the plan covers add, edit, explicit delete with inline confirmation, swipe delete with undo, snackbar undo, selected-date timestamping, current-clock default time, no date reassignment, Material time picker, text-only empty state, accessibility strings, and Maestro coverage.
- No database migration is planned because `DrinkEntity` is unchanged.
- The only planned dependency addition is `kotlinx-coroutines-test`, and only if the ViewModel tests reveal it is missing.
- The plan intentionally keeps Home's existing `AddDrinkDialog` unchanged. Reuse is limited to concepts and any small local components the implementer chooses to share without broad refactors.
