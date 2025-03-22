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
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CalendarScreenState(
    val drinks: List<DrinkItem> = emptyList(),
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

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val drinksRepository: DrinksRepository
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    val state: StateFlow<CalendarScreenState> = drinksRepository.getAllDrinks()
        .map { drinks ->
            CalendarScreenState(
                drinks = drinks.map { it.toDrinkItem() },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarScreenState()
        )

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
