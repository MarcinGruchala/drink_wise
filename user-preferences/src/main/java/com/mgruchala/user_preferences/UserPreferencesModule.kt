package com.mgruchala.user_preferences

import android.content.Context
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesRepository
import com.mgruchala.user_preferences.alcohol_limit.AlcoholLimitPreferencesRepositoryImpl
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
    fun provideAlcoholLimitPreferencesRepository(
        @ApplicationContext context: Context
    ): AlcoholLimitPreferencesRepository {
        return AlcoholLimitPreferencesRepositoryImpl(context)
    }
} 
