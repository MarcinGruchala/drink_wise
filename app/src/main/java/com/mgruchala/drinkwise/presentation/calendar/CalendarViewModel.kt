package com.mgruchala.drinkwise.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.drinkwise.utils.time.Clock
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarScreenState(
    val calendarData: Map<YearMonth, List<CalendarDayData>> = emptyMap(),
    val monthlyAlcoholUnitLevels: Map<YearMonth, AlcoholUnitLevel> = emptyMap(),
    val isLoading: Boolean = true
)

data class CalendarDayData(
    val date: LocalDate,
    val alcoholUnitLevel: AlcoholUnitLevel?
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    drinksRepository: DrinksRepository,
    alcoholLimitPreferencesRepository: AlcoholLimitPreferencesDataSource,
    private val clock: Clock
) : ViewModel() {

    private val drinksFlow = drinksRepository.getAllDrinks()
    private val userPreferencesFlow = alcoholLimitPreferencesRepository.preferences

    val state: StateFlow<CalendarScreenState> =
        combine(drinksFlow, userPreferencesFlow) { drinks, userPreferences ->
            val processedData = processCalendarData(
                drinks = drinks,
                dailyAlcoholLimit = userPreferences.dailyAlcoholUnitLimit.coerceAtLeast(0.1f),
                monthlyAlcoholLimit = userPreferences.monthlyAlcoholUnitLimit.coerceAtLeast(0.1f)
            )
            CalendarScreenState(
                calendarData = processedData.calendarData,
                monthlyAlcoholUnitLevels = processedData.monthlyAlcoholUnitLevels,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarScreenState()
        )

    private fun processCalendarData(
        drinks: List<DrinkEntity>,
        dailyAlcoholLimit: Float,
        monthlyAlcoholLimit: Float
    ): CalendarProcessedData {
        val drinksByDate = drinks.groupBy {
            timestampToLocalDate(it.timestamp)
        }

        val today = currentLocalDate()
        val startDate = if (drinks.isEmpty()) {
            today.minusMonths(1).withDayOfMonth(1)
        } else {
            drinks.minByOrNull { it.timestamp }?.let {
                timestampToLocalDate(it.timestamp)
            } ?: today.minusMonths(1).withDayOfMonth(1)
        }

        val calendarDays = mutableListOf<CalendarDayData>()
        var currentDate = startDate

        while (!currentDate.isAfter(today)) {
            val drinksForDay = drinksByDate[currentDate] ?: emptyList()
            val dayAlcoholUnits = drinksForDay.sumOf {
                calculateAlcoholUnits(volumeMl = it.quantity, abv = it.alcoholContent)
            }

            calendarDays.add(
                CalendarDayData(
                    date = currentDate,
                    alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(
                        dayAlcoholUnits.toFloat(),
                        dailyAlcoholLimit
                    ),
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        val calendarData = calendarDays.groupBy { YearMonth.from(it.date) }
            .toSortedMap(compareByDescending { it })
        val monthlyAlcoholUnitLevels = calendarData.mapValues { (_, days) ->
            val monthlyUnitCount = days.sumOf { day ->
                day.alcoholUnitLevel?.unitCount?.toDouble() ?: 0.0
            }.toFloat()
            AlcoholUnitLevel.fromUnitCount(monthlyUnitCount, monthlyAlcoholLimit)
        }.toSortedMap(compareByDescending { it })

        return CalendarProcessedData(
            calendarData = calendarData,
            monthlyAlcoholUnitLevels = monthlyAlcoholUnitLevels
        )
    }

    private fun timestampToLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    private fun currentLocalDate(): LocalDate {
        return timestampToLocalDate(clock.nowMillis())
    }
} 

private data class CalendarProcessedData(
    val calendarData: Map<YearMonth, List<CalendarDayData>>,
    val monthlyAlcoholUnitLevels: Map<YearMonth, AlcoholUnitLevel>
)
