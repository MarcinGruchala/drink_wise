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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Delete
    suspend fun deleteDrink(drink: DrinkEntity): Int
}
