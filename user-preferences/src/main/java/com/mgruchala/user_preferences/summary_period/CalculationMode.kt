package com.mgruchala.user_preferences.summary_period

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class CalculationMode {
    @SerialName("since_start")
    SINCE_START_OF_PERIOD,

    @SerialName("rolling")
    ROLLING_PERIOD
}
