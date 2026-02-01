package com.mgruchala.drinkwise.presentation.daydetails.components

fun formatVolume(volumeMl: Int): String {
    return if (volumeMl >= 1000) {
        val liters = volumeMl / 1000f
        if (liters == liters.toLong().toFloat()) {
            "${liters.toLong()} L"
        } else {
            "${"%.1f".format(liters)} L"
        }
    } else {
        "$volumeMl ml"
    }
}
