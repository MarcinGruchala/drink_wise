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
    drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val epochDay: Long = savedStateHandle.get<Long>(AppRoute.DayDetails.ARG_EPOCH_DAY) ?: 0L
    private val selectedDate: LocalDate = LocalDate.ofEpochDay(epochDay)

    private val drinksFlow = drinksRepository.getDrinksForDate(selectedDate)
    private val preferencesFlow = alcoholLimitPreferencesDataSource.preferences

    val state: StateFlow<DayDetailsState> = combine(
        drinksFlow,
        preferencesFlow
    ) { drinks, preferences ->
        val totalUnits = drinks.sumOf {
            calculateAlcoholUnits(it.quantity, it.alcoholContent)
        }.toFloat()
        val dailyLimit = preferences.dailyAlcoholUnitLimit

        DayDetailsState(
            selectedDate = selectedDate,
            drinks = drinks,
            totalUnits = totalUnits,
            dailyLimit = dailyLimit,
            alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(totalUnits, dailyLimit),
            isLoading = false,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayDetailsState(selectedDate = selectedDate)
    )
}
