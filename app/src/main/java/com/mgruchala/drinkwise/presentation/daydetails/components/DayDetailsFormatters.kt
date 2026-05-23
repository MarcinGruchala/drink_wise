package com.mgruchala.drinkwise.presentation.daydetails.components

import com.mgruchala.drinkwise.presentation.common.formatAlcoholUnitsCompact
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDayDetailsUnits(value: Float): String = formatAlcoholUnitsCompact(value)

fun formatDayDetailsVolume(volumeMl: Int): String {
    return if (volumeMl >= 1000) {
        val liters = volumeMl / 1000f
        if (liters == liters.toInt().toFloat()) {
            "${liters.toInt()} L"
        } else {
            String.format(Locale.US, "%.1f L", liters)
        }
    } else {
        "$volumeMl ml"
    }
}

fun formatDayDetailsAbv(abv: Float): String {
    return String.format(Locale.US, "%.1f%%", abv)
}

fun formatDayDetailsTime(timestamp: Long): String {
    val localTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
}
