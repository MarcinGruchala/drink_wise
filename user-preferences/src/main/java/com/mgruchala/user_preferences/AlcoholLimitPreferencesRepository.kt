package com.mgruchala.user_preferences

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing alcohol limit user preferences.
 */
interface AlcoholLimitPreferencesRepository {

    /**
     * Flow of user alcohol limit preferences.
     */
    val userPreferencesFlow: Flow<AlcoholLimitPreferences>

    /**
     * Update daily alcohol limit
     */
    suspend fun updateDailyAlcoholLimit(limit: Float)

    /**
     * Update weekly alcohol limit
     */
    suspend fun updateWeeklyAlcoholLimit(limit: Float)

    /**
     * Update monthly alcohol limit
     */
    suspend fun updateMonthlyAlcoholLimit(limit: Float)
}
