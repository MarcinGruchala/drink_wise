package com.mgruchala.user_preferences

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlcoholLimitPreferencesModule {

    @Provides
    @Singleton
    fun provideAlcoholLimitPreferencesRepository(
        @ApplicationContext context: Context
    ): AlcoholLimitPreferencesRepository {
        return AlcoholLimitPreferencesRepositoryImpl(context)
    }
} 
