package com.mgruchala.alcohol_database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlcoholDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AlcoholDatabase {
        return Room.databaseBuilder(
            context,
            AlcoholDatabase::class.java,
            AlcoholDatabase.NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDrinkDao(appDatabase: AlcoholDatabase): DrinkDao {
        return appDatabase.drinkDao()
    }
}
