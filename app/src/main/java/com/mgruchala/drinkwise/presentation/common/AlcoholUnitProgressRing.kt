package com.mgruchala.drinkwise.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

private const val StartAngleDegrees = -90f
private const val MinimumAlcoholUnitIndicatorLimit = 0.1f

// Extra clear radius as a fraction of indicator diameter; 4dp on the 220dp details ring.
const val AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction = 0.025f

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

internal fun calculateAlcoholUnitIndicatorOverflowGapRadius(
    strokeWidth: Float,
    indicatorDiameter: Float,
    overflowGapPaddingFraction: Float
): Float {
    val strokeRadius = (strokeWidth / 2f).coerceAtLeast(0f)
    val gapPadding = (indicatorDiameter * overflowGapPaddingFraction).coerceAtLeast(0f)
    return strokeRadius + gapPadding
}

internal fun alcoholUnitLevelIndicatorColor(alcoholUnitLevel: AlcoholUnitLevel): Color {
    return when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }
}

@Composable
internal fun AlcoholUnitProgressRing(
    alcoholUnitLevel: AlcoholUnitLevel,
    trackColor: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
    overflowGapPaddingFraction: Float = AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction
) {
    val ratio = calculateAlcoholUnitIndicatorRatio(
        unitCount = alcoholUnitLevel.unitCount,
        limit = alcoholUnitLevel.limit
    )
    val levelColor = alcoholUnitLevelIndicatorColor(alcoholUnitLevel)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.99f }
    ) {
        drawAlcoholUnitProgressRing(
            ratio = ratio,
            levelColor = levelColor,
            trackColor = trackColor,
            strokeWidth = strokeWidth.toPx(),
            overflowGapPaddingFraction = overflowGapPaddingFraction
        )
    }
}

private fun DrawScope.drawAlcoholUnitProgressRing(
    ratio: Float,
    levelColor: Color,
    trackColor: Color,
    strokeWidth: Float,
    overflowGapPaddingFraction: Float
) {
    val indicatorDiameter = min(size.width, size.height)
    val outerRadius = indicatorDiameter / 2f - strokeWidth / 2f
    if (outerRadius <= 0f) {
        return
    }

    if (ratio <= 1f) {
        drawCircle(
            color = trackColor,
            radius = outerRadius,
            style = Stroke(width = strokeWidth)
        )

        drawAlcoholUnitProgress(
            color = levelColor,
            radius = outerRadius,
            strokeWidth = strokeWidth,
            progress = calculateAlcoholUnitIndicatorBaseProgress(ratio)
        )
    } else {
        drawCircle(
            color = levelColor,
            radius = outerRadius,
            style = Stroke(width = strokeWidth)
        )

        val overflowProgress = calculateAlcoholUnitIndicatorOverflowProgress(ratio)
        if (overflowProgress > 0f && overflowProgress < 1f) {
            val endAngleDegrees = StartAngleDegrees + (360f * overflowProgress)
            val endAngleRad = Math.toRadians(endAngleDegrees.toDouble())
            val headX = center.x + outerRadius * cos(endAngleRad).toFloat()
            val headY = center.y + outerRadius * sin(endAngleRad).toFloat()

            drawCircle(
                color = Color.Black,
                radius = calculateAlcoholUnitIndicatorOverflowGapRadius(
                    strokeWidth = strokeWidth,
                    indicatorDiameter = indicatorDiameter,
                    overflowGapPaddingFraction = overflowGapPaddingFraction
                ),
                center = Offset(headX, headY),
                blendMode = BlendMode.Clear
            )

            drawAlcoholUnitProgress(
                color = levelColor,
                radius = outerRadius,
                strokeWidth = strokeWidth,
                progress = overflowProgress
            )
        }
    }
}

private fun DrawScope.drawAlcoholUnitProgress(
    color: Color,
    radius: Float,
    strokeWidth: Float,
    progress: Float
) {
    when {
        progress >= 1f -> drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        progress > 0f -> drawArc(
            color = color,
            startAngle = StartAngleDegrees,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
