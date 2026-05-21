package com.mgruchala.drinkwise.presentation.home

import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import java.util.Locale

internal fun AlcoholUnitLevel.formattedUnitCount(): String {
    return unitCount.formatHomeAlcoholUnits()
}

internal fun AlcoholUnitLevel.formattedLimit(): String {
    return limit.formatHomeAlcoholUnits()
}

private fun Float.formatHomeAlcoholUnits(): String {
    return String.format(Locale.US, "%.2f", this)
}
