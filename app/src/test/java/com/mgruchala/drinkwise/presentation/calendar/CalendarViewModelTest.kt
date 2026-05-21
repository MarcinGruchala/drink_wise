package com.mgruchala.drinkwise.presentation.calendar

import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.time.Clock
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
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val zoneId = ZoneId.systemDefault()
    private val today = LocalDate.of(2026, 5, 21)

    @BeforeEach
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state exposes monthly alcohol unit totals using monthly limit`() = runTest(testDispatcher) {
        val repository = FakeDrinksRepository()
        val preferencesDataSource = FakeAlcoholLimitPreferencesDataSource(
            preferences = AlcoholLimitPreferences(
                dailyAlcoholUnitLimit = 4f,
                weeklyAlcoholUnitLimit = 14f,
                monthlyAlcoholUnitLimit = 10f
            )
        )
        val viewModel = CalendarViewModel(
            drinksRepository = repository,
            alcoholLimitPreferencesRepository = preferencesDataSource,
            clock = FakeClock(timestampFor(today, LocalTime.NOON))
        )
        val stateCollection = backgroundScope.launch { viewModel.state.collect { } }

        repository.setDrinks(
            listOf(
                DrinkEntity(
                    uid = 1,
                    quantity = 500,
                    alcoholContent = 5f,
                    timestamp = timestampFor(LocalDate.of(2026, 5, 1), LocalTime.of(20, 0))
                ),
                DrinkEntity(
                    uid = 2,
                    quantity = 250,
                    alcoholContent = 12f,
                    timestamp = timestampFor(LocalDate.of(2026, 5, 20), LocalTime.of(21, 0))
                ),
                DrinkEntity(
                    uid = 3,
                    quantity = 100,
                    alcoholContent = 10f,
                    timestamp = timestampFor(LocalDate.of(2026, 4, 30), LocalTime.of(19, 0))
                )
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals(
            AlcoholUnitLevel.Low(unitCount = 5.5f, limit = 10f),
            viewModel.state.value.monthlyAlcoholUnitLevels.getValue(YearMonth.of(2026, 5))
        )
        assertEquals(
            AlcoholUnitLevel.Low(unitCount = 1f, limit = 10f),
            viewModel.state.value.monthlyAlcoholUnitLevels.getValue(YearMonth.of(2026, 4))
        )
        stateCollection.cancel()
    }

    private fun timestampFor(date: LocalDate, time: LocalTime): Long {
        return date.atTime(time)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private class FakeClock(private val nowMillis: Long) : Clock {
        override fun nowMillis(): Long = nowMillis
    }

    private class FakeDrinksRepository : DrinksRepository {
        private val drinks = MutableStateFlow<List<DrinkEntity>>(emptyList())

        fun setDrinks(value: List<DrinkEntity>) {
            drinks.value = value
        }

        override fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>> = drinks
        override fun getAllDrinks(): Flow<List<DrinkEntity>> = drinks
        override fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>> = drinks
        override suspend fun addDrinks(vararg drinks: DrinkEntity) = Unit
        override suspend fun updateDrink(drink: DrinkEntity): Int = 0
        override suspend fun deleteDrink(drink: DrinkEntity): Int = 0
    }

    private class FakeAlcoholLimitPreferencesDataSource(
        preferences: AlcoholLimitPreferences
    ) : AlcoholLimitPreferencesDataSource {
        override val preferences: Flow<AlcoholLimitPreferences> = MutableStateFlow(preferences)

        override suspend fun updateDailyAlcoholLimit(limit: Float) = Unit
        override suspend fun updateWeeklyAlcoholLimit(limit: Float) = Unit
        override suspend fun updateMonthlyAlcoholLimit(limit: Float) = Unit
    }
}
