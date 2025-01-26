package com.mgruchala.drinkwise.calculator

/**
 * Calculates the number of alcohol units in a drink.
 *
 * @param volumeMl The volume of the drink in milliliters.
 * @param abv The Alcohol by Volume (ABV) percentage of the drink.
 * @return The number of alcohol units.
 */
fun calculateAlcoholUnits(volumeMl: Double, abv: Double): Float {
    return (volumeMl * abv).toFloat() / 1000
}
