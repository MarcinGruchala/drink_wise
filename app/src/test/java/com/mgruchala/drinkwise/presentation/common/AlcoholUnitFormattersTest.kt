package com.mgruchala.drinkwise.presentation.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AlcoholUnitFormattersTest {

    @Test
    fun `formats alcohol units with two decimal places`() {
        assertEquals("3.32", 3.3173332f.formatAlcoholUnits())
        assertEquals("45.08", 45.077873f.formatAlcoholUnits())
        assertEquals("14.00", 14f.formatAlcoholUnits())
    }
}
