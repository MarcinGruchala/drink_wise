package com.mgruchala.drinkwise.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.user_preferences.AlcoholLimitPreferencesRepository
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
    val isLoading: Boolean = true
)

data class CalendarDayData(
    val date: LocalDate,
    val alcoholUnitLevel: AlcoholUnitLevel?
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    drinksRepository: DrinksRepository,
    alcoholLimitPreferencesRepository: AlcoholLimitPreferencesRepository
) : ViewModel() {

    private val drinksFlow = drinksRepository.getAllDrinks()
    private val userPreferencesFlow = alcoholLimitPreferencesRepository.userPreferencesFlow

    val state: StateFlow<CalendarScreenState> =
        combine(drinksFlow, userPreferencesFlow) { drinks, userPreferences ->
            val calendarData = processCalendarData(
                drinks = drinks,
                dailyAlcoholLimit = userPreferences.dailyAlcoholUnitLimit
            )
            CalendarScreenState(
                calendarData = calendarData,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarScreenState()
        )

    private fun processCalendarData(drinks: List<DrinkEntity>, dailyAlcoholLimit: Float): Map<YearMonth, List<CalendarDayData>> {
        val drinksByDate = drinks.groupBy {
            timestampToLocalDate(it.timestamp)
        }

        val today = LocalDate.now()
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

        return calendarDays.groupBy { YearMonth.from(it.date) }
            .toSortedMap(compareByDescending { it })
    }

    private fun timestampToLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
} 
