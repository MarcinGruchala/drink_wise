package com.mgruchala.drinkwise.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CalendarScreenState(
    val drinks: List<DrinkItem> = emptyList(),
    val calendarData: Map<YearMonth, List<CalendarDayData>> = emptyMap(),
    val isLoading: Boolean = true
)

data class DrinkItem(
    val id: Int,
    val quantity: Int,
    val alcoholContent: Float,
    val timestamp: Long,
    val formattedDate: String,
    val alcoholUnits: Double
)

data class CalendarDayData(
    val date: LocalDate,
    val hasDrinks: Boolean,
    val drinkCount: Int = 0
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val drinksRepository: DrinksRepository
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    val state: StateFlow<CalendarScreenState> = drinksRepository.getAllDrinks()
        .map { drinks ->
            val drinkItems = drinks.map { it.toDrinkItem() }
            val calendarData = processCalendarData(drinks)
            
            CalendarScreenState(
                drinks = drinkItems,
                calendarData = calendarData,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarScreenState()
        )

    private fun processCalendarData(drinks: List<DrinkEntity>): Map<YearMonth, List<CalendarDayData>> {
        // Group drinks by day
        val drinksByDate = drinks.groupBy {
            timestampToLocalDate(it.timestamp)
        }
        
        // Get range of dates
        val today = LocalDate.now()
        val startDate = if (drinks.isEmpty()) {
            today.minusMonths(1).withDayOfMonth(1)
        } else {
            drinks.minByOrNull { it.timestamp }?.let {
                timestampToLocalDate(it.timestamp)
            } ?: today.minusMonths(1).withDayOfMonth(1)
        }
        
        // Create calendar days data
        val calendarDays = mutableListOf<CalendarDayData>()
        var currentDate = startDate
        
        // Generate all days between start date and today
        while (!currentDate.isAfter(today)) {
            val drinksForDay = drinksByDate[currentDate] ?: emptyList()
            calendarDays.add(
                CalendarDayData(
                    date = currentDate,
                    hasDrinks = drinksForDay.isNotEmpty(),
                    drinkCount = drinksForDay.size
                )
            )
            currentDate = currentDate.plusDays(1)
        }
        
        // Group by month for display
        return calendarDays.groupBy { YearMonth.from(it.date) }
            .toSortedMap(compareByDescending { it })
    }
    
    private fun timestampToLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    private fun DrinkEntity.toDrinkItem(): DrinkItem {
        val date = Date(timestamp)
        return DrinkItem(
            id = uid,
            quantity = quantity,
            alcoholContent = alcoholContent,
            timestamp = timestamp,
            formattedDate = dateFormatter.format(date),
            alcoholUnits = calculateAlcoholUnits(quantity, alcoholContent)
        )
    }
    
    fun deleteDrink(drinkId: Int) {
        viewModelScope.launch {
            val drinkToDelete = state.value.drinks.find { it.id == drinkId } ?: return@launch
            val drinkEntity = DrinkEntity(
                uid = drinkToDelete.id,
                quantity = drinkToDelete.quantity,
                alcoholContent = drinkToDelete.alcoholContent,
                timestamp = drinkToDelete.timestamp
            )
            drinksRepository.deleteDrink(drinkEntity)
        }
    }
} 
