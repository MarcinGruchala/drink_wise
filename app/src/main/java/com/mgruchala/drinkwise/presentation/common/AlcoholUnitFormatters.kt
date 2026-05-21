package com.mgruchala.drinkwise.presentation.common

import java.util.Locale

internal fun Float.formatAlcoholUnits(): String {
    return String.format(Locale.US, "%.2f", this)
}
