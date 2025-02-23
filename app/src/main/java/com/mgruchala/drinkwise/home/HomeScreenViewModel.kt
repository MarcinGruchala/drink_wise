package com.mgruchala.drinkwise.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        HomeScreenState(
            todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(6f, 4f),
            weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(12f, 14f),
            monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(15f, 30f),
        )
    )

    val state: StateFlow<HomeScreenState>
        get() = _state
}


data class HomeScreenState(
    val todayAlcoholUnitLevel: AlcoholUnitLevel,
    val weekAlcoholUnitLevel: AlcoholUnitLevel,
    val monthAlcoholUnitLevel: AlcoholUnitLevel,
)

data class DrinkSummary(
    val title: String,
    val alcoholUnitCount: Float,
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
