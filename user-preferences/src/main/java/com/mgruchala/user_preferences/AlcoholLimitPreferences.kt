package com.mgruchala.user_preferences

import kotlinx.serialization.Serializable

/**
 * Data class representing user preferences for alcohol consumption limits.
 */
@Serializable
data class AlcoholLimitPreferences(
    val dailyAlcoholUnitLimit: Float,
    val weeklyAlcoholUnitLimit: Float,
    val monthlyAlcoholUnitLimit: Float
)
