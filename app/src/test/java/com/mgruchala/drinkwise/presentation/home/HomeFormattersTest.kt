package com.mgruchala.drinkwise.presentation.home

import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HomeFormattersTest {

    @Test
    fun `formats alcohol level values with two decimal places`() {
        val alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(3.3173332f, 14f)

        assertEquals("3.32", alcoholUnitLevel.formattedUnitCount())
        assertEquals("14.00", alcoholUnitLevel.formattedLimit())
    }

    @Test
    fun `formats rounded alcohol level values with two decimal places`() {
        val alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(45.077873f, 30f)

        assertEquals("45.08", alcoholUnitLevel.formattedUnitCount())
        assertEquals("30.00", alcoholUnitLevel.formattedLimit())
    }
}
