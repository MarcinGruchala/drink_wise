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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DayDetailsViewModel @Inject constructor(
    private val drinksRepository: DrinksRepository,
    alcoholLimitPreferencesDataSource: AlcoholLimitPreferencesDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val epochDay: Long = savedStateHandle.get<Long>(AppRoute.DayDetails.ARG_EPOCH_DAY) ?: LocalDate.now().toEpochDay()
    private val initialDate: LocalDate = LocalDate.ofEpochDay(epochDay)

    private val _selectedDate = MutableStateFlow(initialDate)
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val preferencesFlow = alcoholLimitPreferencesDataSource.preferences

    val state: StateFlow<DayDetailsState> = combine(
        _selectedDate.flatMapLatest { date ->
            drinksRepository.getDrinksForDate(date)
        },
        preferencesFlow,
        _selectedDate
    ) { drinks, preferences, date ->
        val totalUnits = drinks.sumOf {
            calculateAlcoholUnits(it.quantity, it.alcoholContent)
        }.toFloat()
        val dailyLimit = preferences.dailyAlcoholUnitLimit

        DayDetailsState(
            selectedDate = date,
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
        initialValue = DayDetailsState(selectedDate = initialDate)
    )

    /**
     * Change the selected date for viewing drinks.
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /**
     * Get the initial date from navigation.
     */
    fun getInitialDate(): LocalDate = initialDate
}
