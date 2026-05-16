package com.mgruchala.drinkwise.utils.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class LocalDateRangeTest {

    @Test
    fun `creates inclusive local day range`() {
        val zoneId = ZoneId.of("Europe/Warsaw")
        val range = LocalDate.of(2026, 5, 16).toEpochMillisRange(zoneId)

        val expectedStart = LocalDate.of(2026, 5, 16)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val expectedEnd = LocalDate.of(2026, 5, 17)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli() - 1

        assertEquals(expectedStart, range.startMillis)
        assertEquals(expectedEnd, range.endMillis)
    }
}
