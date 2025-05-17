package com.mgruchala.user_preferences.alcohol_limit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlcoholLimitPreferencesDataSourceImpl @Inject constructor(
    private val context: Context
) : AlcoholLimitPreferencesDataSource {

    private object PreferencesKeys {
        val DAILY_ALCOHOL_LIMIT = floatPreferencesKey("daily_alcohol_limit")
        val WEEKLY_ALCOHOL_LIMIT = floatPreferencesKey("weekly_alcohol_limit")
        val MONTHLY_ALCOHOL_LIMIT = floatPreferencesKey("monthly_alcohol_limit")
    }

    override val preferences: Flow<AlcoholLimitPreferences> = context.dataStore.data
        .map { preferences ->
            AlcoholLimitPreferences(
                dailyAlcoholUnitLimit = preferences[PreferencesKeys.DAILY_ALCOHOL_LIMIT]
                    ?: DEFAULT_DAILY_LIMIT,
                weeklyAlcoholUnitLimit = preferences[PreferencesKeys.WEEKLY_ALCOHOL_LIMIT]
                    ?: DEFAULT_WEEKLY_LIMIT,
                monthlyAlcoholUnitLimit = preferences[PreferencesKeys.MONTHLY_ALCOHOL_LIMIT]
                    ?: DEFAULT_MONTHLY_LIMIT
            )
        }

    override suspend fun updateDailyAlcoholLimit(limit: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_ALCOHOL_LIMIT] = limit
        }
    }

    override suspend fun updateWeeklyAlcoholLimit(limit: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_ALCOHOL_LIMIT] = limit
        }
    }

    override suspend fun updateMonthlyAlcoholLimit(limit: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_ALCOHOL_LIMIT] = limit
        }
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "alcohol_limit_preferences"
        )
        private const val DEFAULT_DAILY_LIMIT = 7f
        private const val DEFAULT_WEEKLY_LIMIT = 14f
        private const val DEFAULT_MONTHLY_LIMIT = 30f
    }
} 
