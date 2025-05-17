package com.mgruchala.user_preferences.summary_period

import kotlinx.serialization.Serializable


/**
 * Data class representing user preferences for summary card calculation period.
 */
@Serializable
data class SummaryPeriodPreferences(
    val dailySummaryCalculationPeriod: CalculationMode,
    val weeklySummaryCalculationPeriod: CalculationMode,
    val monthlySummaryCalculationPeriod: CalculationMode
)
