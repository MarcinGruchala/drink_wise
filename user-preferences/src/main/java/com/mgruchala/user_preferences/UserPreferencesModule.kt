package com.mgruchala.user_preferences

import android.content.Context
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSource
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesDataSourceImpl
import com.mgruchala.user_preferences.summary_period.SummaryPeriodPreferencesDataSource
import com.mgruchala.user_preferences.summary_period.SummaryPeriodPreferencesDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserPreferencesModule {

    @Provides
    @Singleton
    fun provideAlcoholLimitPreferencesDataSource(
        @ApplicationContext context: Context
    ): AlcoholLimitPreferencesDataSource {
        return AlcoholLimitPreferencesDataSourceImpl(context)
    }

    @Provides
    @Singleton
    fun provideSummaryPeriodPreferencesDataSource(
        @ApplicationContext context: Context
    ): SummaryPeriodPreferencesDataSource {
        return SummaryPeriodPreferencesDataSourceImpl(context)
    }
} 
