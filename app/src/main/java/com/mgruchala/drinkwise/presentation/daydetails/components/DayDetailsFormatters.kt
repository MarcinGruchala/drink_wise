package com.mgruchala.drinkwise.presentation.daydetails.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun formatDayDetailsUnits(value: Float): String {
    val roundedToTenth = (value * 10f).roundToInt() / 10f
    return if (roundedToTenth == roundedToTenth.toInt().toFloat()) {
        roundedToTenth.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", roundedToTenth)
    }
}

fun formatDayDetailsVolume(volumeMl: Int): String {
    return if (volumeMl >= 1000) {
        val liters = volumeMl / 1000f
        if (liters == liters.toInt().toFloat()) {
            "${liters.toInt()} L"
        } else {
            String.format(Locale.getDefault(), "%.1f L", liters)
        }
    } else {
        "$volumeMl ml"
    }
}

fun formatDayDetailsAbv(abv: Float): String {
    return String.format(Locale.getDefault(), "%.1f%%", abv)
}

fun formatDayDetailsTime(timestamp: Long): String {
    val localTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
}
