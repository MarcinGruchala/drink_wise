package com.mgruchala.alcohol_database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DrinkEntity::class], version = 1)
abstract class AlcoholDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao

    companion object {
        const val NAME = "alcohol_database"
    }
}
