package com.mgruchala.user_preferences.summary_period

import kotlinx.coroutines.flow.Flow

interface SummaryPeriodPreferencesRepository {

    /**
     * Flow of user summary period preferences.
     */
    val userPreferencesFlow: Flow<SummaryPeriodPreferences>

    /**
     * Update daily summary calculation period.
     */
    suspend fun updateDailySummaryCalculationPeriod(calculationMode: CalculationMode)

    /**
     * Update weekly summary calculation period.
     */
    suspend fun updateWeeklySummaryCalculationPeriod(calculationMode: CalculationMode)

    /**
     * Update monthly summary calculation period.
     */
    suspend fun updateMonthlySummaryCalculationPeriod(calculationMode: CalculationMode)
}
