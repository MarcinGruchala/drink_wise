package com.mgruchala.drinkwise.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.user_preferences.AlcoholLimitPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val dailyLimit: Float = 7f,
    val weeklyLimit: Float = 14f,
    val monthlyLimit: Float = 30f,
    val isSaving: Boolean = false,
    val showSuccessMessage: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: AlcoholLimitPreferencesRepository
) : ViewModel() {

    private val _savingState = MutableStateFlow(false)
    private val _showSuccessMessage = MutableStateFlow(false)

    val state: StateFlow<SettingsState> = combine(
        preferencesRepository.userPreferencesFlow,
        _savingState,
        _showSuccessMessage
    ) { preferences, isSaving, showSuccess ->
        SettingsState(
            dailyLimit = preferences.dailyAlcoholUnitLimit,
            weeklyLimit = preferences.weeklyAlcoholUnitLimit,
            monthlyLimit = preferences.monthlyAlcoholUnitLimit,
            isSaving = isSaving,
            showSuccessMessage = showSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState()
    )

    fun updateDailyLimit(limit: Float) {
        viewModelScope.launch {
            _savingState.value = true
            preferencesRepository.updateDailyAlcoholLimit(limit)
            _savingState.value = false
            showSuccessMessage()
        }
    }

    fun updateWeeklyLimit(limit: Float) {
        viewModelScope.launch {
            _savingState.value = true
            preferencesRepository.updateWeeklyAlcoholLimit(limit)
            _savingState.value = false
            showSuccessMessage()
        }
    }

    fun updateMonthlyLimit(limit: Float) {
        viewModelScope.launch {
            _savingState.value = true
            preferencesRepository.updateMonthlyAlcoholLimit(limit)
            _savingState.value = false
            showSuccessMessage()
        }
    }

    private fun showSuccessMessage() {
        _showSuccessMessage.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _showSuccessMessage.value = false
        }
    }

    fun dismissSuccessMessage() {
        _showSuccessMessage.value = false
    }
} 
