package com.mgruchala.user_preferences.summary_period

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class SummaryPeriodPreferencesDataSourceImpl @Inject constructor(
    private val context: Context
) : SummaryPeriodPreferencesDataSource {

    private object PreferencesKeys {
        val DAILY_SUMMARY_CALCULATION_PERIOD = stringPreferencesKey("daily_summary_calculation_period")
        val WEEKLY_SUMMARY_CALCULATION_PERIOD = stringPreferencesKey("weekly_summary_calculation_period")
        val MONTHLY_SUMMARY_CALCULATION_PERIOD = stringPreferencesKey("monthly_summary_calculation_period")
    }

    override val preferences: Flow<SummaryPeriodPreferences> = context.dataStore.data
        .map { preferences ->
            SummaryPeriodPreferences(
                dailySummaryCalculationPeriod = CalculationMode.valueOf(
                    preferences[PreferencesKeys.DAILY_SUMMARY_CALCULATION_PERIOD]
                        ?: DEFAULT_CALCULATION_MODE.name
                ),
                weeklySummaryCalculationPeriod = CalculationMode.valueOf(
                    preferences[PreferencesKeys.WEEKLY_SUMMARY_CALCULATION_PERIOD]
                        ?: DEFAULT_CALCULATION_MODE.name
                ),
                monthlySummaryCalculationPeriod = CalculationMode.valueOf(
                    preferences[PreferencesKeys.MONTHLY_SUMMARY_CALCULATION_PERIOD]
                        ?: DEFAULT_CALCULATION_MODE.name
                )
            )
        }

    override suspend fun updateDailySummaryCalculationPeriod(calculationMode: CalculationMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_SUMMARY_CALCULATION_PERIOD] = calculationMode.name
        }
    }

    override suspend fun updateWeeklySummaryCalculationPeriod(calculationMode: CalculationMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_CALCULATION_PERIOD] = calculationMode.name
        }
    }

    override suspend fun updateMonthlySummaryCalculationPeriod(calculationMode: CalculationMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_SUMMARY_CALCULATION_PERIOD] = calculationMode.name
        }
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "summary_period_preferences"
        )
        private val DEFAULT_CALCULATION_MODE = CalculationMode.SINCE_START_OF_PERIOD
    }
}
