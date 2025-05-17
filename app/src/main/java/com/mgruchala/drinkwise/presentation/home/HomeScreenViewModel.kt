package com.mgruchala.drinkwise.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.drinkwise.utils.time.Clock
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import com.mgruchala.user_preferences.summary_period.CalculationMode
import com.mgruchala.user_preferences.summary_period.SummaryPeriodPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel @Inject constructor(
    private val drinksRepository: DrinksRepository,
    alcoholLimitPreferencesRepository: AlcoholLimitPreferencesDataSource,
    private val summaryPeriodPreferencesRepository: SummaryPeriodPreferencesDataSource,
    private val clock: Clock
) : ViewModel() {

    private val alcoholLimitPreferencesFlow = alcoholLimitPreferencesRepository.preferences

    private val summaryPeriodPreferencesFlow =
        summaryPeriodPreferencesRepository.preferences.shareIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1
        )

    private fun drinksSince(periodMillis: Long, mode: CalculationMode): Flow<List<DrinkEntity>> =
        drinksRepository.getDrinksSince(
            calculateCutoff(clock.nowMillis(), periodMillis, mode)
        )

    private val todayFlow = summaryPeriodPreferencesFlow.flatMapLatest { prefs ->
        drinksSince(ONE_DAY_IN_MILLIS, prefs.dailySummaryCalculationPeriod)
    }
    private val weekFlow = summaryPeriodPreferencesFlow.flatMapLatest { prefs ->
        drinksSince(SEVEN_DAYS_IN_MILLIS, prefs.weeklySummaryCalculationPeriod)
    }
    private val monthFlow = summaryPeriodPreferencesFlow.flatMapLatest { prefs ->
        drinksSince(THIRTY_DAYS_IN_MILLIS, prefs.monthlySummaryCalculationPeriod)
    }

    private fun Flow<List<DrinkEntity>>.unitCount(): Flow<Float> =
        map { list ->
            list.sumOf { calculateAlcoholUnits(it.quantity, it.alcoholContent) }.toFloat()
        }

    val state: StateFlow<HomeScreenState> = combine(
        todayFlow.unitCount(),
        weekFlow.unitCount(),
        monthFlow.unitCount(),
        alcoholLimitPreferencesFlow,
        summaryPeriodPreferencesFlow
    ) { todayUnits, weekUnits, monthUnits, alcoholLimitPreferences, summaryPeriodPreferences ->

        HomeScreenState(
            todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                todayUnits,
                alcoholLimitPreferences.dailyAlcoholUnitLimit
            ),
            weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                weekUnits,
                alcoholLimitPreferences.weeklyAlcoholUnitLimit
            ),
            monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                monthUnits,
                alcoholLimitPreferences.monthlyAlcoholUnitLimit
            ),
            dailySummaryCalculationMode = summaryPeriodPreferences.dailySummaryCalculationPeriod,
            weeklySummaryCalculationMode = summaryPeriodPreferences.weeklySummaryCalculationPeriod,
            monthlySummaryCalculationMode = summaryPeriodPreferences.monthlySummaryCalculationPeriod,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeScreenState(
                todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(0f, 4f),
                weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(0f, 7f),
                monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(0f, 21f),
                dailySummaryCalculationMode = CalculationMode.ROLLING_PERIOD,
                weeklySummaryCalculationMode = CalculationMode.ROLLING_PERIOD,
                monthlySummaryCalculationMode = CalculationMode.SINCE_START_OF_PERIOD
            )
        )

    fun registerNewDrinks(quantity: Int, abv: Float, numberOfDrinks: Int) {
        viewModelScope.launch {
            val drinks = mutableListOf<DrinkEntity>()
            val timestampForAllDrinks = clock.nowMillis()
            for (i in 1..numberOfDrinks) {
                val newDrink = DrinkEntity(
                    uid = 0,
                    quantity = quantity,
                    alcoholContent = abv,
                    timestamp = timestampForAllDrinks
                )
                drinks.add(newDrink)
            }
            drinksRepository.addDrinks(*drinks.toTypedArray())
        }
    }

    private fun calculateCutoff(
        currentTime: Long,
        periodType: Long,
        calculationMode: CalculationMode
    ): Long {
        return when (calculationMode) {
            CalculationMode.ROLLING_PERIOD -> {
                currentTime - periodType
            }

            CalculationMode.SINCE_START_OF_PERIOD -> {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = currentTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                when (periodType) {
                    ONE_DAY_IN_MILLIS -> {
                        calendar.timeInMillis
                    }

                    SEVEN_DAYS_IN_MILLIS -> {
                        calendar.firstDayOfWeek = Calendar.MONDAY
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        calendar.timeInMillis
                    }

                    THIRTY_DAYS_IN_MILLIS -> {
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        calendar.timeInMillis
                    }

                    else -> {
                        currentTime - periodType
                    }
                }
            }
        }
    }

    fun updateDailySummaryCalculationModePreferences(calculationMode: CalculationMode) {
        viewModelScope.launch {
            summaryPeriodPreferencesRepository.updateDailySummaryCalculationPeriod(calculationMode)
        }
    }

    fun updateWeeklySummaryCalculationModePreferences(calculationMode: CalculationMode) {
        viewModelScope.launch {
            summaryPeriodPreferencesRepository.updateWeeklySummaryCalculationPeriod(calculationMode)
        }
    }

    fun updateMonthlySummaryCalculationModePreferences(calculationMode: CalculationMode) {
        viewModelScope.launch {
            summaryPeriodPreferencesRepository.updateMonthlySummaryCalculationPeriod(calculationMode)
        }
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val SEVEN_DAYS_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS
        private const val THIRTY_DAYS_IN_MILLIS = 30 * ONE_DAY_IN_MILLIS
    }
}

data class HomeScreenState(
    val todayAlcoholUnitLevel: AlcoholUnitLevel,
    val weekAlcoholUnitLevel: AlcoholUnitLevel,
    val monthAlcoholUnitLevel: AlcoholUnitLevel,
    val dailySummaryCalculationMode: CalculationMode,
    val weeklySummaryCalculationMode: CalculationMode,
    val monthlySummaryCalculationMode: CalculationMode
)
