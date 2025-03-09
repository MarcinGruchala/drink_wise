package com.mgruchala.alcohol_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface DrinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: DrinkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>): List<Long>

    @Delete
    suspend fun deleteDrink(drink: DrinkEntity): Int

    @Query("SELECT * FROM drinks")
    suspend fun getAllDrinks(): List<DrinkEntity>

    @Query("SELECT * FROM drinks ORDER BY timestamp DESC")
    suspend fun getDrinksOrderByTimestampDesc(): List<DrinkEntity>
}
