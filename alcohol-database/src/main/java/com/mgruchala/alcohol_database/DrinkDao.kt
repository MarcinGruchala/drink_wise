package com.mgruchala.alcohol_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drinks WHERE timestamp >= :cutoff ORDER BY timestamp DESC")
    fun getDrinksSince(cutoff: Long): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks ORDER BY timestamp DESC")
    fun getAllDrinks(): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getPaginatedDrinksByDateRange(startDate: Long, endDate: Long): Flow<List<DrinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Delete
    suspend fun deleteDrink(drink: DrinkEntity): Int

    @Query("SELECT COUNT(*) FROM drinks")
    suspend fun getDrinkCount(): Int
}
