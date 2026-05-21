package com.mgruchala.drinkwise.presentation.common

import java.util.Locale
import kotlin.math.roundToInt

internal fun Float.formatAlcoholUnits(): String {
    return String.format(Locale.US, "%.2f", this)
}

fun formatAlcoholUnitsCompact(value: Float): String {
    val roundedToTenth = (value * 10f).roundToInt() / 10f
    return if (roundedToTenth == roundedToTenth.toInt().toFloat()) {
        roundedToTenth.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", roundedToTenth)
    }
}
