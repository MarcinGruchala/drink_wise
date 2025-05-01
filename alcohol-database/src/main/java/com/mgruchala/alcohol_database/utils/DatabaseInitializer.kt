package com.mgruchala.alcohol_database.utils

import android.util.Log
import com.mgruchala.alcohol_database.BuildConfig
import com.mgruchala.alcohol_database.DrinkDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class to initialize database with test data in debug builds
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val drinkDao: DrinkDao
) {
    private val tag = "DatabaseInitializer"
    private var initialized = false

    /**
     * Initializes the database with random drinks if in debug mode
     * Safe to call multiple times - will only populate once
     */
    fun initializeIfNeeded() {
        if (!BuildConfig.DEBUG) {
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if database is empty
                val existingCount = withContext(Dispatchers.IO) {
                    drinkDao.getDrinkCount()
                }
                
                if (existingCount == 0) {
                    Log.d(tag, "Database empty, populating with random data")
                    val randomDrinks = createRandomDrinkEntities(monthsBack = 3, drinksPerMonth = 25)
                    drinkDao.insertDrinks(randomDrinks)
                    Log.d(tag, "Successfully inserted ${randomDrinks.size} random drinks")
                } else {
                    Log.d(tag, "Database already contains $existingCount drinks, skipping initialization")
                }
                
                initialized = true
            } catch (e: Exception) {
                Log.e(tag, "Error initializing database with test data", e)
            }
        }
    }
} 
