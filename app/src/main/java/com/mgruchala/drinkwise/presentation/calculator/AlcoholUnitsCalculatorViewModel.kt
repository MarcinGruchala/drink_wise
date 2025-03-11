package com.mgruchala.drinkwise.presentation.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AlcoholCalculatorState(
    val drinkQuantityMl: Int? = null,
    val alcoholPercentage: Float? = null,
    val amountOfDrinks: Int = 1,
    val calculatedUnits: Float? = null
)

fun AlcoholCalculatorState.canAddDrink(): Boolean {
    return drinkQuantityMl != null && alcoholPercentage != null
}

@HiltViewModel
class AlcoholUnitsCalculatorViewModel @Inject constructor() : ViewModel() {
    private val _drinkQuantityMl = MutableStateFlow<Int?>(null)
    private val _alcoholPercentage = MutableStateFlow<Float?>(null)
    private val _amountOfDrinks = MutableStateFlow(1)

    val state: StateFlow<AlcoholCalculatorState> = combine(
        _drinkQuantityMl,
        _alcoholPercentage,
        _amountOfDrinks
    ) { quantity, abv, amount ->
        val units = if (quantity != null && abv != null) {
            calculateAlcoholUnits(quantity, abv) * amount
        } else {
            null
        }
        AlcoholCalculatorState(
            drinkQuantityMl = quantity,
            alcoholPercentage = abv,
            amountOfDrinks = amount,
            calculatedUnits = units?.toFloat()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlcoholCalculatorState()
        )


    fun onQuantityChanged(newValue: Int) {
        _drinkQuantityMl.value = newValue
    }

    fun onPercentageChanged(newValue: Float) {
        _alcoholPercentage.value = newValue.coerceIn(0f, 100f)
    }

    fun onIncrement() {
        _amountOfDrinks.value += 1
    }

    fun onDecrement() {
        val current = _amountOfDrinks.value
        if (current > 1) {
            _amountOfDrinks.value = current - 1
        }
    }

    fun resetAlcoholCalculator() {
        _drinkQuantityMl.value = null
        _alcoholPercentage.value = null
        _amountOfDrinks.value = 1
    }

}
