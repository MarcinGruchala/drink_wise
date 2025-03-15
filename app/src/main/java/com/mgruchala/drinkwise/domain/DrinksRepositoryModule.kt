package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DrinksRepositoryModule {

    @Provides
    @Singleton
    fun provideDrinksRepository(
        drinkDao: DrinkDao
    ): DrinksRepository {
        return DrinksRepositoryImpl(drinkDao)
    }
}
