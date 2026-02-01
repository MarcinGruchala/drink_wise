package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkDao
import com.mgruchala.alcohol_database.DrinkEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class DrinksRepositoryImpl(
    private val drinkDao: DrinkDao
) : DrinksRepository {

    override fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>> {
        return drinkDao.getDrinksSince(cutoff)
    }

    override fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>> {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return drinkDao.getPaginatedDrinksByDateRange(startOfDay, endOfDay)
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
}
