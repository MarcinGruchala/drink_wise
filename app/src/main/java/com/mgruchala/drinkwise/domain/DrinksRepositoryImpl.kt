package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkDao
import com.mgruchala.alcohol_database.DrinkEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

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
    
    override fun getAllDrinks(): Flow<List<DrinkEntity>> {
        return drinkDao.getAllDrinks()
    }

    override suspend fun addDrinks(vararg drinks: DrinkEntity) {
        drinkDao.insertDrinks(drinks.toList())
    }
    
    override suspend fun deleteDrink(drink: DrinkEntity): Int {
        return drinkDao.deleteDrink(drink)
    }

    override fun getDrinksForMonth(year: Int, month: Int): Flow<List<DrinkEntity>> {
        val calendar = Calendar.getInstance()
        
        // Set start date to first day of month at 00:00:00
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis
        
        // Set end date to last day of month at 23:59:59
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.timeInMillis
        
        return drinkDao.getPaginatedDrinksByDateRange(startDate, endDate)
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val SEVEN_DAYS_IN_MILLIS = 7 * ONE_DAY_IN_MILLIS
        private const val THIRTY_DAYS_IN_MILLIS = 30 * ONE_DAY_IN_MILLIS
    }
}
