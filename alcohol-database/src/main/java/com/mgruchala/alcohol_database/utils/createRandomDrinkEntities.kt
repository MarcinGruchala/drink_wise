package com.mgruchala.alcohol_database.utils

import com.mgruchala.alcohol_database.DrinkEntity
import java.util.Calendar
import java.util.Random

fun createRandomDrinkEntities(monthsBack: Int = 6, drinksPerMonth: Int = 20): List<DrinkEntity> {
    val calendar = Calendar.getInstance()
    val endTime = calendar.timeInMillis
    calendar.add(Calendar.MONTH, -monthsBack)
    val startTime = calendar.timeInMillis
    
    val random = Random()
    val drinks = mutableListOf<DrinkEntity>()
    
    val timeRange = endTime - startTime
    val avgTimeBetweenDrinks = timeRange / (monthsBack * drinksPerMonth)

    for (i in 0 until monthsBack * drinksPerMonth) {
        val randomOffset = (random.nextDouble() * 0.5 + 0.75) * avgTimeBetweenDrinks
        val timestamp = startTime + (i * avgTimeBetweenDrinks + randomOffset.toLong())
        
        // Determine drink type and corresponding values
        val drinkType = random.nextInt(3)
        
        // Set appropriate quantity (ml) and alcohol content based on drink type
        val (quantity, alcoholContent) = when (drinkType) {
            0 -> {
                // Beer (330-500ml, 4-6%)
                Pair(
                    330 + random.nextInt(171),
                    4.0f + random.nextFloat() * 2.0f
                )
            }
            1 -> {
                // Wine (150-250ml, 11-15%)
                Pair(
                    150 + random.nextInt(101),
                    11.0f + random.nextFloat() * 4.0f
                )
            }
            else -> {
                // Spirits/shots (40-60ml, 35-40%)
                Pair(
                    40 + random.nextInt(21),
                    35.0f + random.nextFloat() * 5.0f
                )
            }
        }
        
        drinks.add(
            DrinkEntity(
                uid = 0,
                quantity = quantity,
                alcoholContent = alcoholContent,
                timestamp = timestamp
            )
        )
    }

    return drinks.sortedBy { it.timestamp }
}
