package com.mgruchala.drinkwise.presentation.daydetails.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DayDetailsFormattersTest {

    @Test
    fun `formats units without decimals for whole values`() {
        assertEquals("4", formatDayDetailsUnits(4f))
    }

    @Test
    fun `formats units with one decimal for fractional values`() {
        assertEquals("4.2", formatDayDetailsUnits(4.24f))
    }

    @Test
    fun `formats milliliters below one liter`() {
        assertEquals("330 ml", formatDayDetailsVolume(330))
    }

    @Test
    fun `formats liters at and above one liter`() {
        assertEquals("1.5 L", formatDayDetailsVolume(1500))
    }

    @Test
    fun `formats abv with percent sign`() {
        assertEquals("5.2%", formatDayDetailsAbv(5.2f))
    }
}
