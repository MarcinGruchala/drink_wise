package com.mgruchala.drinkwise.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import com.mgruchala.user_preferences.summary_period.CalculationMode
import com.mgruchala.user_preferences.summary_period.SummaryPeriodPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val drinksRepository: DrinksRepository,
    alcoholLimitPreferencesRepository: AlcoholLimitPreferencesDataSource,
    private val summaryPeriodPreferencesRepository: SummaryPeriodPreferencesDataSource
) : ViewModel() {

    private val alcoholLimitPreferencesFlow = alcoholLimitPreferencesRepository.preferences
    private val summaryPeriodPreferencesFlow =
        summaryPeriodPreferencesRepository.preferences

    private val todayFlow = summaryPeriodPreferencesFlow.flatMapLatest { summaryPrefs ->
        val cutoff = calculateCutoff(
            System.currentTimeMillis(),
            ONE_DAY_IN_MILLIS,
            summaryPrefs.dailySummaryCalculationPeriod
        )
        drinksRepository.getDrinksSince(cutoff)
    }

    private val weekFlow = summaryPeriodPreferencesFlow.flatMapLatest { summaryPrefs ->
        val cutoff = calculateCutoff(
            System.currentTimeMillis(),
            SEVEN_DAYS_IN_MILLIS,
            summaryPrefs.weeklySummaryCalculationPeriod
        )
        drinksRepository.getDrinksSince(cutoff)
    }

    private val monthFlow = summaryPeriodPreferencesFlow.flatMapLatest { summaryPrefs ->
        val cutoff = calculateCutoff(
            System.currentTimeMillis(),
            THIRTY_DAYS_IN_MILLIS,
            summaryPrefs.monthlySummaryCalculationPeriod
        )
        drinksRepository.getDrinksSince(cutoff)
    }

    val state: StateFlow<HomeScreenState> = combine(
        todayFlow,
        weekFlow,
        monthFlow,
        alcoholLimitPreferencesFlow,
        summaryPeriodPreferencesFlow
    ) { todayDrinks, weekDrinks, monthDrinks, alcoholLimitPreferences, summaryPeriodPreferences ->

        val todayUnits = todayDrinks.sumOf {
            calculateAlcoholUnits(volumeMl = it.quantity, abv = it.alcoholContent)
        }

        val weekUnits = weekDrinks.sumOf {
            calculateAlcoholUnits(volumeMl = it.quantity, abv = it.alcoholContent)
        }

        val monthUnits = monthDrinks.sumOf {
            calculateAlcoholUnits(volumeMl = it.quantity, abv = it.alcoholContent)
        }

        HomeScreenState(
            todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                todayUnits.toFloat(),
                alcoholLimitPreferences.dailyAlcoholUnitLimit
            ),
            weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                weekUnits.toFloat(),
                alcoholLimitPreferences.weeklyAlcoholUnitLimit
            ),
            monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                monthUnits.toFloat(),
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
            val timestampForAllDrinks = System.currentTimeMillis()
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
