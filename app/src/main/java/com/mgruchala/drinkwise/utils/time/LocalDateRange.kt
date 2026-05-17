package com.mgruchala.drinkwise.utils.time

import java.time.LocalDate
import java.time.ZoneId

data class EpochMillisRange(
    val startMillis: Long,
    val endMillis: Long
)

fun LocalDate.toEpochMillisRange(zoneId: ZoneId = ZoneId.systemDefault()): EpochMillisRange {
    val startMillis = atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val endMillis = plusDays(1)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli() - 1
    return EpochMillisRange(startMillis, endMillis)
}
