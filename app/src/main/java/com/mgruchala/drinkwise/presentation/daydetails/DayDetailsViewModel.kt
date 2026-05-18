package com.mgruchala.drinkwise.presentation.daydetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.navigaiton.AppRoute
import com.mgruchala.drinkwise.presentation.daydetails.editor.composeDrinkTimestamp
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class DayDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource
) : ViewModel() {

    private val selectedDate = LocalDate.ofEpochDay(
        savedStateHandle[AppRoute.DayDetails.ARG_EPOCH_DAY] ?: LocalDate.now().toEpochDay()
    )
    private var lastDeletedDrink: DrinkEntity? = null

    val state: StateFlow<DayDetailsState> = combine(
        drinksRepository.getDrinksForDate(selectedDate),
        alcoholLimitPreferencesDataSource.preferences
    ) { drinks, preferences ->
        val totalUnits = drinks.sumOf { drink ->
            calculateAlcoholUnits(drink.quantity, drink.alcoholContent)
        }.toFloat()
        val dailyLimit = preferences.dailyAlcoholUnitLimit.coerceAtLeast(0.1f)

        DayDetailsState(
            selectedDate = selectedDate,
            drinks = drinks,
            totalUnits = totalUnits,
            dailyLimit = dailyLimit,
            alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(totalUnits, dailyLimit),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayDetailsState(selectedDate = selectedDate)
    )

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
            val deletedRows = drinksRepository.deleteDrink(drink)
            lastDeletedDrink = if (deletedRows > 0) {
                drink
            } else {
                null
            }
        }
    }

    fun undoLastDeletedDrink() {
        viewModelScope.launch {
            val drink = lastDeletedDrink ?: return@launch
            drinksRepository.addDrinks(drink.copy(uid = 0))
            lastDeletedDrink = null
        }
    }
}
