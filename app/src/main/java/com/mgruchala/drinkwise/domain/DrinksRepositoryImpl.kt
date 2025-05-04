package com.mgruchala.drinkwise.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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

    override fun getDrinksByDateRangePaginated(
        startTimestamp: Long,
        endTimestamp: Long,
        pageSize: Int
    ): Flow<PagingData<DrinkEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            )
        ) {
            drinkDao.getPaginatedDrinksByDateRange(startTimestamp, endTimestamp)
        }.flow
    }
    
    override fun getAllDrinks(): Flow<List<DrinkEntity>> {
        return drinkDao.getAllDrinks()
    }

    override suspend fun addDrinks(vararg drinks: DrinkEntity) {
        drinkDao.insertDrinks(drinks.toList())
    }
    
    override suspend fun deleteDrink(drink: DrinkEntity): Int {
        return drinkDao.deleteDrink(drink)
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val SEVEN_DAYS_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS
        private const val THIRTY_DAYS_IN_MILLIS = 30 * ONE_DAY_IN_MILLIS
    }
}
