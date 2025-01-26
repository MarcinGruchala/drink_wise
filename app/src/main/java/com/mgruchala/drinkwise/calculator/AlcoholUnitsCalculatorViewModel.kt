package com.mgruchala.drinkwise.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AlcoholCalculatorState(
    val drinkQuantityMl: Int? = null,
    val alcoholPercentage: Float? = null,
    val amountOfDrinks: Int = 1,
    val calculatedUnits: Float? = null
)

class AlcoholUnitsCalculatorViewModel : ViewModel() {
    private val _drinkQuantityMl = MutableStateFlow<Int?>(null)
    private val _alcoholPercentage = MutableStateFlow<Float?>(null)
    private val _amountOfDrinks = MutableStateFlow(1)

    val state: StateFlow<AlcoholCalculatorState> = combine(
        _drinkQuantityMl,
        _alcoholPercentage,
        _amountOfDrinks
    ) { quantity, abv, amount ->
        val units = if (quantity != null && abv != null) {
            calculateAlcoholUnits(quantity.toDouble(), abv.toDouble()) * amount
        } else {
            null
        }
        AlcoholCalculatorState(
            drinkQuantityMl = quantity,
            alcoholPercentage = abv,
            amountOfDrinks = amount,
            calculatedUnits = units
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

}
