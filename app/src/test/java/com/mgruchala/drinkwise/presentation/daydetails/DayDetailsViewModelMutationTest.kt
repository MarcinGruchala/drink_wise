package com.mgruchala.drinkwise.presentation.daydetails

import androidx.lifecycle.SavedStateHandle
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.navigaiton.AppRoute
import com.mgruchala.drinkwise.presentation.daydetails.editor.composeDrinkTimestamp
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferences
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun `delete stores pending drink and undo restores it once`() = runTest(testDispatcher) {
        val repository = FakeDrinksRepository()
        val viewModel = createViewModel(repository)
        val drink = DrinkEntity(uid = 3, quantity = 175, alcoholContent = 13.5f, timestamp = 123L)
        repository.setDrinks(listOf(drink))
        val stateCollection = backgroundScope.launch { viewModel.state.collect { } }
        testScheduler.advanceUntilIdle()

        viewModel.deleteDrink(drink)
        testScheduler.advanceUntilIdle()

        assertEquals(drink, repository.deletedDrink)
        assertEquals(emptyList<DrinkEntity>(), viewModel.state.value.drinks)

        viewModel.undoLastDeletedDrink()
        testScheduler.advanceUntilIdle()

        val restored = repository.addedDrinks.single()
        assertEquals(0, restored.uid)
        assertEquals(drink.quantity, restored.quantity)
        assertEquals(drink.alcoholContent, restored.alcoholContent)
        assertEquals(drink.timestamp, restored.timestamp)
        val visibleRestored = viewModel.state.value.drinks.single()
        assertEquals(drink.quantity, visibleRestored.quantity)
        assertEquals(drink.alcoholContent, visibleRestored.alcoholContent)
        assertEquals(drink.timestamp, visibleRestored.timestamp)

        viewModel.undoLastDeletedDrink()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.addedDrinks.size)
        assertEquals(visibleRestored, viewModel.state.value.drinks.single())
        stateCollection.cancel()
    }

    @Test
    fun `undo does not restore drink when delete affects no rows`() = runTest(testDispatcher) {
        val repository = FakeDrinksRepository(deleteResult = 0)
        val viewModel = createViewModel(repository)
        val drink = DrinkEntity(uid = 3, quantity = 175, alcoholContent = 13.5f, timestamp = 123L)

        viewModel.deleteDrink(drink)
        testScheduler.advanceUntilIdle()

        assertEquals(drink, repository.deletedDrink)

        viewModel.undoLastDeletedDrink()
        testScheduler.advanceUntilIdle()

        assertEquals(0, repository.addedDrinks.size)
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

    private class FakeDrinksRepository(
        private val deleteResult: Int = 1
    ) : DrinksRepository {
        private val drinks = MutableStateFlow<List<DrinkEntity>>(emptyList())
        private var nextUid = 100
        val addedDrinks = mutableListOf<DrinkEntity>()
        var updatedDrink: DrinkEntity? = null
        var deletedDrink: DrinkEntity? = null

        fun setDrinks(value: List<DrinkEntity>) {
            drinks.value = value
            nextUid = (value.maxOfOrNull { it.uid } ?: 99) + 1
        }

        override fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>> = drinks
        override fun getAllDrinks(): Flow<List<DrinkEntity>> = drinks
        override fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>> = drinks

        override suspend fun addDrinks(vararg drinks: DrinkEntity) {
            val insertedDrinks = drinks.map { drink ->
                if (drink.uid == 0) {
                    drink.copy(uid = nextUid++)
                } else {
                    drink
                }
            }
            addedDrinks += drinks
            this.drinks.value += insertedDrinks
        }

        override suspend fun updateDrink(drink: DrinkEntity): Int {
            updatedDrink = drink
            return 1
        }

        override suspend fun deleteDrink(drink: DrinkEntity): Int {
            deletedDrink = drink
            if (deleteResult <= 0) {
                return deleteResult
            }
            val beforeDelete = drinks.value
            val afterDelete = beforeDelete.filterNot { it.uid == drink.uid }
            drinks.value = afterDelete
            return beforeDelete.size - afterDelete.size
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
