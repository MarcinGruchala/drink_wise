package com.mgruchala.drinkwise.utils

/**
 * Calculates the number of alcohol units in a drink.
 *
 * @param volumeMl The volume of the drink in milliliters.
 * @param abv The Alcohol by Volume (ABV) percentage of the drink.
 * @return The number of alcohol units.
 */
fun calculateAlcoholUnits(volumeMl: Int, abv: Float): Double {
    return (volumeMl * abv).toDouble() / 1000
}
