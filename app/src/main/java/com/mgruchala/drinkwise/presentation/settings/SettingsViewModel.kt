package com.mgruchala.drinkwise.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LimitType {
    DAILY, WEEKLY, MONTHLY
}

data class SettingsState(
    val dailyLimit: Float = 7f,
    val weeklyLimit: Float = 14f,
    val monthlyLimit: Float = 30f,
    val savingLimits: Set<LimitType> = emptySet(),
    val showSuccessMessage: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: AlcoholLimitPreferencesRepository
) : ViewModel() {

    private val _savingLimits = MutableStateFlow<Set<LimitType>>(emptySet())
    private val _showSuccessMessage = MutableStateFlow(false)

    val state: StateFlow<SettingsState> = combine(
        preferencesRepository.userPreferencesFlow,
        _savingLimits,
        _showSuccessMessage
    ) { preferences, savingLimits, showSuccess ->
        SettingsState(
            dailyLimit = preferences.dailyAlcoholUnitLimit,
            weeklyLimit = preferences.weeklyAlcoholUnitLimit,
            monthlyLimit = preferences.monthlyAlcoholUnitLimit,
            savingLimits = savingLimits,
            showSuccessMessage = showSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState()
    )

    fun updateDailyLimit(limit: Float) {
        viewModelScope.launch {
            _savingLimits.value += LimitType.DAILY
            preferencesRepository.updateDailyAlcoholLimit(limit)
            _savingLimits.value -= LimitType.DAILY
            showSuccessMessage()
        }
    }

    fun updateWeeklyLimit(limit: Float) {
        viewModelScope.launch {
            _savingLimits.value += LimitType.WEEKLY
            preferencesRepository.updateWeeklyAlcoholLimit(limit)
            _savingLimits.value -= LimitType.WEEKLY
            showSuccessMessage()
        }
    }

    fun updateMonthlyLimit(limit: Float) {
        viewModelScope.launch {
            _savingLimits.value += LimitType.MONTHLY
            preferencesRepository.updateMonthlyAlcoholLimit(limit)
            _savingLimits.value -= LimitType.MONTHLY
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
