package com.mgruchala.drinkwise.presentation.daydetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.domain.DrinksRepository
import com.mgruchala.drinkwise.navigaiton.AppRoute
import com.mgruchala.drinkwise.utils.calculateAlcoholUnits
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DayDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource
) : ViewModel() {

    private val selectedDate = LocalDate.ofEpochDay(
        savedStateHandle[AppRoute.DayDetails.ARG_EPOCH_DAY] ?: LocalDate.now().toEpochDay()
    )

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
}
