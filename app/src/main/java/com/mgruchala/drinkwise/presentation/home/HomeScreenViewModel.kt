package com.mgruchala.drinkwise.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val drinksRepository: DrinksRepository
) : ViewModel() {

    private val todayFlow = drinksRepository.getDrinksLast24Hours()
    private val weekFlow = drinksRepository.getDrinksLast7Days()
    private val monthFlow = drinksRepository.getDrinksLast30Days()

    val state: StateFlow<HomeScreenState> = combine(
        todayFlow,
        weekFlow,
        monthFlow
    ) { todayDrinks, weekDrinks, monthDrinks ->

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
            todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(todayUnits.toFloat(), 4f),
            weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(weekUnits.toFloat(), 7f),
            monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(monthUnits.toFloat(), 21f),
        )
    }
        // Convert the combined Flow into a StateFlow, providing an initial state.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000), // or SharingStarted.Eagerly, etc.
            initialValue = HomeScreenState(
                todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(0f, 4f),
                weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(0f, 7f),
                monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(0f, 21f),
            )
        )
}

data class HomeScreenState(
    val todayAlcoholUnitLevel: AlcoholUnitLevel,
    val weekAlcoholUnitLevel: AlcoholUnitLevel,
    val monthAlcoholUnitLevel: AlcoholUnitLevel,
)

sealed class AlcoholUnitLevel(open val unitCount: Float, open val limit: Float) {
    data class Low(
        override val unitCount: Float,
        override val limit: Float
    ) : AlcoholUnitLevel(unitCount, limit)

    data class Alarming(
        override val unitCount: Float,
        override val limit: Float
    ) : AlcoholUnitLevel(unitCount, limit)

    data class High(
        override val unitCount: Float,
        override val limit: Float
    ) : AlcoholUnitLevel(unitCount, limit)

    companion object {
        fun fromUnitCount(unitCount: Float, limit: Float): AlcoholUnitLevel {
            return when {
                unitCount <= 0.7 * limit -> Low(unitCount, limit)
                unitCount < limit -> Alarming(unitCount, limit)
                else -> High(unitCount, limit)
            }
        }
    }
}
