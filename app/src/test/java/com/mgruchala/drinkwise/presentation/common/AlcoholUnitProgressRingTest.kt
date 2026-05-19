package com.mgruchala.drinkwise.presentation.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AlcoholUnitProgressRingTest {

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
    }

    @Test
    fun `safe limit keeps positive configured limits`() {
        val limit = calculateAlcoholUnitIndicatorSafeLimit(limit = 4f)

        assertEquals(4f, limit, FLOAT_TOLERANCE)
    }

    @Test
    fun `safe limit clamps zero limits`() {
        val limit = calculateAlcoholUnitIndicatorSafeLimit(limit = 0f)

        assertEquals(0.1f, limit, FLOAT_TOLERANCE)
    }

    @Test
    fun `safe limit clamps negative limits`() {
        val limit = calculateAlcoholUnitIndicatorSafeLimit(limit = -2f)

        assertEquals(0.1f, limit, FLOAT_TOLERANCE)
    }

    @Test
    fun `ratio uses the safe limit`() {
        val ratio = calculateAlcoholUnitIndicatorRatio(unitCount = 4f, limit = 0f)

        assertEquals(40f, ratio, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress follows consumption up to the limit`() {
        val progress = calculateAlcoholUnitIndicatorBaseProgress(ratio = 0.41f)

        assertEquals(0.41f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress is capped at the limit`() {
        val progress = calculateAlcoholUnitIndicatorBaseProgress(ratio = 1.14f)

        assertEquals(1f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `base progress does not render negative consumption`() {
        val progress = calculateAlcoholUnitIndicatorBaseProgress(ratio = -0.25f)

        assertEquals(0f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress is absent up to the limit`() {
        val belowLimitProgress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 0.41f)
        val atLimitProgress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 1f)

        assertEquals(0f, belowLimitProgress, FLOAT_TOLERANCE)
        assertEquals(0f, atLimitProgress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress follows the current lap after the limit`() {
        val progress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 1.14f)

        assertEquals(0.14f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress preserves the current lap for large overages`() {
        val progress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 7.41f)

        assertEquals(0.41f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow progress renders a full lap for exact over-limit cycles`() {
        val progress = calculateAlcoholUnitIndicatorOverflowProgress(ratio = 3f)

        assertEquals(1f, progress, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow gap radius preserves the large day detail ring spacing`() {
        val gapRadius = calculateAlcoholUnitIndicatorOverflowGapRadius(
            strokeWidth = 12f,
            indicatorDiameter = 220f,
            overflowGapPaddingFraction = AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction
        )

        assertEquals(10f, gapRadius, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow gap radius scales down for compact indicators`() {
        val gapRadius = calculateAlcoholUnitIndicatorOverflowGapRadius(
            strokeWidth = 3f,
            indicatorDiameter = 54f,
            overflowGapPaddingFraction = AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction
        )

        assertEquals(2.4818f, gapRadius, FLOAT_TOLERANCE)
    }

    @Test
    fun `overflow gap radius ignores negative extra padding factors`() {
        val gapRadius = calculateAlcoholUnitIndicatorOverflowGapRadius(
            strokeWidth = 3f,
            indicatorDiameter = 54f,
            overflowGapPaddingFraction = -0.2f
        )

        assertEquals(1.5f, gapRadius, FLOAT_TOLERANCE)
    }
}
