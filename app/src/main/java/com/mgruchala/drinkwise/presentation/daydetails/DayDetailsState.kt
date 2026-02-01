package com.mgruchala.drinkwise.presentation.daydetails

import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import java.time.LocalDate

data class DayDetailsState(
    val selectedDate: LocalDate = LocalDate.now(),
    val drinks: List<DrinkEntity> = emptyList(),
    val totalUnits: Float = 0f,
    val dailyLimit: Float = 7f,
    val alcoholUnitLevel: AlcoholUnitLevel = AlcoholUnitLevel.Low(0f, 7f),
    val isLoading: Boolean = true,
    val error: String? = null
)
