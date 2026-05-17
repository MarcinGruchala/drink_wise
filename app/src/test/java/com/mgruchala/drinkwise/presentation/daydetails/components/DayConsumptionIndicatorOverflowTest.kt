package com.mgruchala.drinkwise.presentation.daydetails.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DayConsumptionIndicatorOverflowTest {

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
    }

    @Test
    fun `base progress follows consumption up to the daily limit`() {
        val progress = calculateConsumptionIndicatorBaseProgress(ratio = 0.41f)

        assertEquals(0.41f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress is capped at the daily limit`() {
        val progress = calculateConsumptionIndicatorBaseProgress(ratio = 1.14f)

        assertEquals(1f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress does not render negative consumption`() {
        val progress = calculateConsumptionIndicatorBaseProgress(ratio = -0.25f)

        assertEquals(0f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress is absent up to the daily limit`() {
        val belowLimitProgress = calculateConsumptionIndicatorOverflowProgress(ratio = 0.41f)
        val atLimitProgress = calculateConsumptionIndicatorOverflowProgress(ratio = 1f)

        assertEquals(0f, belowLimitProgress, FLOAT_TOLERANCE)
        assertEquals(0f, atLimitProgress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress follows the current lap after the daily limit`() {
        val progress = calculateConsumptionIndicatorOverflowProgress(ratio = 1.14f)

        assertEquals(0.14f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress preserves the current lap for large overages`() {
        val progress = calculateConsumptionIndicatorOverflowProgress(ratio = 7.41f)

        assertEquals(0.41f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress renders a full lap for exact over-limit cycles`() {
        val progress = calculateConsumptionIndicatorOverflowProgress(ratio = 3f)

        assertEquals(1f, progress, FLOAT_TOLERANCE)
    }
}
