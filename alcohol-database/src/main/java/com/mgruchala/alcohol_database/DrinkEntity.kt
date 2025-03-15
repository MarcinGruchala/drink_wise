package com.mgruchala.alcohol_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int,
    val quantity: Int,
    @ColumnInfo("alcohol_content")
    val alcoholContent: Float,
    val timestamp: Long
)

