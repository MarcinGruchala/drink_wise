package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkDao
import com.mgruchala.alcohol_database.DrinkEntity
import kotlinx.coroutines.flow.Flow

class DrinksRepositoryImpl(
    private val drinkDao: DrinkDao
) : DrinksRepository {

    override fun getDrinksLast24Hours(): Flow<List<DrinkEntity>> {
        val cutoff = System.currentTimeMillis() - ONE_DAY_IN_MILLIS
        return drinkDao.getDrinksSince(cutoff)
    }

    override fun getDrinksLast7Days(): Flow<List<DrinkEntity>> {
        val cutoff = System.currentTimeMillis() - SEVEN_DAYS_IN_MILLIS
        return drinkDao.getDrinksSince(cutoff)
    }

    override fun getDrinksLast30Days(): Flow<List<DrinkEntity>> {
        val cutoff = System.currentTimeMillis() - THIRTY_DAYS_IN_MILLIS
        return drinkDao.getDrinksSince(cutoff)
    }

    override suspend fun addDrinks(vararg drinks: DrinkEntity) {
        drinkDao.insertDrinks(drinks.toList())
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val SEVEN_DAYS_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS
        private const val THIRTY_DAYS_IN_MILLIS = 30 * ONE_DAY_IN_MILLIS
    }
}
