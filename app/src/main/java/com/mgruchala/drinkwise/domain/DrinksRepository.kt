package com.mgruchala.drinkwise.domain

import com.mgruchala.alcohol_database.DrinkEntity
import kotlinx.coroutines.flow.Flow

interface DrinksRepository {

    /**
     * Fetches all drinks consumed within the past 24 hours, as a Flow.
     */
    fun getDrinksLast24Hours(): Flow<List<DrinkEntity>>

    /**
     * Fetches all drinks consumed within the past 7 days, as a Flow.
     */
    fun getDrinksLast7Days(): Flow<List<DrinkEntity>>

    /**
     * Fetches all drinks consumed within the past 30 days, as a Flow.
     */
    fun getDrinksLast30Days(): Flow<List<DrinkEntity>>

    /**
     * Inserts one or more drinks of same quantity and alcohol content.
     */
    suspend fun addDrinks(vararg drinks: DrinkEntity)
}

