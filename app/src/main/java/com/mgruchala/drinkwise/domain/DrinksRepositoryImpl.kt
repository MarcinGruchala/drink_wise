package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkDao
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.utils.time.toEpochMillisRange
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DrinksRepositoryImpl(
    private val drinkDao: DrinkDao
) : DrinksRepository {

    override fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>> {
        return drinkDao.getDrinksSince(cutoff)
    }

    override fun getAllDrinks(): Flow<List<DrinkEntity>> {
        return drinkDao.getAllDrinks()
    }

    override fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>> {
        val range = date.toEpochMillisRange()
        return drinkDao.getPaginatedDrinksByDateRange(
            startDate = range.startMillis,
            endDate = range.endMillis
        )
    }

    override suspend fun addDrinks(vararg drinks: DrinkEntity) {
        drinkDao.insertDrinks(drinks.toList())
    }

    override suspend fun updateDrink(drink: DrinkEntity): Int = drinkDao.updateDrink(drink)

    override suspend fun deleteDrink(drink: DrinkEntity): Int {
        return drinkDao.deleteDrink(drink)
    }
}
