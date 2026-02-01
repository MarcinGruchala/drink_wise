package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DrinksRepository {

    /**
     * Fetches all drinks consumed since a given cutoff time, as a Flow.
     */
    fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>>

    /**
     * Fetches all drinks for a specific date, as a Flow.
     */
    fun getDrinksForDate(date: LocalDate): Flow<List<DrinkEntity>>

    /**
     * Fetches all drinks in the database, as a Flow.
     */
    fun getAllDrinks(): Flow<List<DrinkEntity>>

    /**
     * Inserts one or more drinks of same quantity and alcohol content.
     */
    suspend fun addDrinks(vararg drinks: DrinkEntity)

    /**
     * Deletes a drink from the database.
     */
    suspend fun deleteDrink(drink: DrinkEntity): Int
}

