package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkDao
import com.mgruchala.alcohol_database.DrinkEntity
import kotlinx.coroutines.flow.Flow

class DrinksRepositoryImpl(
    private val drinkDao: DrinkDao
) : DrinksRepository {

    override fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>> {
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
}
