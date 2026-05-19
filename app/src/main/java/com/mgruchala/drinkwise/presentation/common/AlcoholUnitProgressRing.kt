package com.mgruchala.drinkwise.presentation.common

import kotlin.math.floor

private const val MinimumAlcoholUnitIndicatorLimit = 0.1f

internal fun calculateAlcoholUnitIndicatorSafeLimit(limit: Float): Float {
    return limit.coerceAtLeast(MinimumAlcoholUnitIndicatorLimit)
}

internal fun calculateAlcoholUnitIndicatorRatio(unitCount: Float, limit: Float): Float {
    return unitCount / calculateAlcoholUnitIndicatorSafeLimit(limit)
}

internal fun calculateAlcoholUnitIndicatorBaseProgress(ratio: Float): Float {
    return ratio.coerceIn(0f, 1f)
}

internal fun calculateAlcoholUnitIndicatorOverflowProgress(ratio: Float): Float {
    if (ratio <= 1f) {
        return 0f
    }

    val currentLapProgress = ratio - floor(ratio)
    return if (currentLapProgress == 0f) 1f else currentLapProgress
}
