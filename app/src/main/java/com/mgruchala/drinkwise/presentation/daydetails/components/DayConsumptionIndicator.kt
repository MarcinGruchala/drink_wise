package com.mgruchala.drinkwise.presentation.daydetails.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlin.math.floor
import kotlin.math.min

private const val START_ANGLE_DEGREES = -90f
private val IndicatorStrokeWidth = 12.dp
private val IndicatorRingGap = 4.dp
private val MinimumIndicatorStrokeWidth = 4.dp
private val MinimumIndicatorRingGap = 2.dp
private val MinimumCenterTextRadius = 56.dp

@Composable
fun DayConsumptionIndicator(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier
) {
    val consumed = alcoholUnitLevel.unitCount
    val limit = alcoholUnitLevel.limit.coerceAtLeast(0.1f)
    val ratio = consumed / limit
    val percent = (ratio * 100f).toInt()
    val consumedText = formatDayDetailsUnits(consumed)
    val limitText = formatDayDetailsUnits(limit)
    val overLimitAmount = (consumed - limit).coerceAtLeast(0f)
    val overLimitText = formatDayDetailsUnits(overLimitAmount)
    val isOverLimit = ratio > 1f

    val levelColor = when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val overflowTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
    val contentDescription = if (isOverLimit) {
        stringResource(
            id = R.string.day_details_consumption_indicator_over_limit_description,
            consumedText,
            limitText,
            percent.toString(),
            overLimitText
        )
    } else {
        stringResource(
            id = R.string.day_details_consumption_indicator_description,
            consumedText,
            limitText,
            percent.toString()
        )
    }
    val consumedOfLimit = stringResource(
        id = R.string.day_details_consumed_of_limit,
        consumedText,
        limitText
    )

    Box(
        modifier = modifier
            .size(220.dp)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val preferredStrokeWidth = IndicatorStrokeWidth.toPx()
            val preferredRingGap = IndicatorRingGap.toPx()
            val minimumStrokeWidth = MinimumIndicatorStrokeWidth.toPx()
            val minimumRingGap = MinimumIndicatorRingGap.toPx()
            val minimumCenterTextRadius = MinimumCenterTextRadius.toPx()
            val outerRadius = min(size.width, size.height) / 2f - preferredStrokeWidth / 2f
            if (outerRadius <= 0f) {
                return@Canvas
            }

            val maxRenderedRings = maxRenderableRingCount(
                outerRadius = outerRadius,
                minimumCenterTextRadius = minimumCenterTextRadius,
                minimumStrokeWidth = minimumStrokeWidth,
                minimumRingGap = minimumRingGap
            )
            val baseRingProgress = calculateBaseConsumptionIndicatorProgress(ratio)
            val overflowRings = calculateConsumptionIndicatorOverflowRings(
                ratio = ratio,
                maxRenderedOverflowRings = maxRenderedRings - 1
            )
            val ringLayout = calculateRingLayout(
                ringCount = overflowRings.size + 1,
                outerRadius = outerRadius,
                minimumCenterTextRadius = minimumCenterTextRadius,
                preferredStrokeWidth = preferredStrokeWidth,
                preferredRingGap = preferredRingGap,
                minimumStrokeWidth = minimumStrokeWidth,
                minimumRingGap = minimumRingGap
            )

            drawConsumptionIndicatorRing(
                radius = outerRadius,
                progress = baseRingProgress,
                color = levelColor,
                trackColor = trackColor,
                strokeWidth = ringLayout.strokeWidth
            )

            overflowRings.forEachIndexed { index, ring ->
                val ringOffset = index + 1
                val radius = outerRadius - ringOffset * (ringLayout.strokeWidth + ringLayout.ringGap)
                drawConsumptionIndicatorRing(
                    radius = radius,
                    progress = ring.progress,
                    color = levelColor,
                    trackColor = overflowTrackColor,
                    strokeWidth = ringLayout.strokeWidth
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = consumedOfLimit,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.day_details_units_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (isOverLimit) {
            Text(
                text = stringResource(R.string.day_details_over_limit_amount, overLimitText),
                modifier = Modifier.align(Alignment.TopEnd),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Preview(
    name = "Light - Low",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DayConsumptionIndicatorLowPreviewLightTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.Low(1f, 4f),
        darkTheme = false
    )
}

@Preview(
    name = "Dark - Low",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DayConsumptionIndicatorLowPreviewDarkTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.Low(1f, 4f),
        darkTheme = true
    )
}

@Preview(
    name = "Light - Alarming",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DayConsumptionIndicatorAlarmingPreviewLightTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.Alarming(3.2f, 4f),
        darkTheme = false
    )
}

@Preview(
    name = "Dark - Alarming",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DayConsumptionIndicatorAlarmingPreviewDarkTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.Alarming(3.2f, 4f),
        darkTheme = true
    )
}

@Preview(
    name = "Light - High",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DayConsumptionIndicatorHighPreviewLightTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.High(5.6f, 4f),
        darkTheme = false
    )
}

@Preview(
    name = "Dark - High",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DayConsumptionIndicatorHighPreviewDarkTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.High(5.6f, 4f),
        darkTheme = true
    )
}

@Preview(
    name = "Light - Overflow",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun DayConsumptionIndicatorOverflowPreviewLightTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.High(14.8f, 4f),
        darkTheme = false
    )
}

@Preview(
    name = "Dark - Overflow",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DayConsumptionIndicatorOverflowPreviewDarkTheme() {
    DayConsumptionIndicatorPreview(
        alcoholUnitLevel = AlcoholUnitLevel.High(14.8f, 4f),
        darkTheme = true
    )
}

@Composable
private fun DayConsumptionIndicatorPreview(
    alcoholUnitLevel: AlcoholUnitLevel,
    darkTheme: Boolean
) {
    DrinkWiseTheme(darkTheme = darkTheme) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            DayConsumptionIndicator(
                alcoholUnitLevel = alcoholUnitLevel,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

internal data class ConsumptionIndicatorRing(
    val progress: Float
)

internal fun calculateBaseConsumptionIndicatorProgress(ratio: Float): Float {
    return sanitizeConsumptionRatio(ratio).coerceAtMost(1f)
}

internal fun calculateConsumptionIndicatorOverflowRings(
    ratio: Float,
    maxRenderedOverflowRings: Int
): List<ConsumptionIndicatorRing> {
    val renderedRingLimit = maxRenderedOverflowRings.coerceAtLeast(0)
    if (renderedRingLimit == 0) {
        return emptyList()
    }

    val overflowRatio = (sanitizeConsumptionRatio(ratio) - 1f).coerceAtLeast(0f)
    if (overflowRatio <= 0f) {
        return emptyList()
    }

    val completedCycles = floor(overflowRatio).toInt()
    val partialCycle = overflowRatio - completedCycles
    val hasPartialCycle = partialCycle > 0f
    val rings = buildList {
        repeat(completedCycles) {
            add(ConsumptionIndicatorRing(progress = 1f))
        }
        if (hasPartialCycle) {
            add(ConsumptionIndicatorRing(progress = partialCycle))
        }
    }

    return rings.takeLast(renderedRingLimit)
}

private data class ConsumptionIndicatorRingLayout(
    val strokeWidth: Float,
    val ringGap: Float
)

private fun maxRenderableRingCount(
    outerRadius: Float,
    minimumCenterTextRadius: Float,
    minimumStrokeWidth: Float,
    minimumRingGap: Float
): Int {
    val availableRadius = (outerRadius - minimumCenterTextRadius).coerceAtLeast(0f)
    return (floor(availableRadius / (minimumStrokeWidth + minimumRingGap)).toInt() + 1)
        .coerceAtLeast(1)
}

private fun calculateRingLayout(
    ringCount: Int,
    outerRadius: Float,
    minimumCenterTextRadius: Float,
    preferredStrokeWidth: Float,
    preferredRingGap: Float,
    minimumStrokeWidth: Float,
    minimumRingGap: Float
): ConsumptionIndicatorRingLayout {
    if (ringCount <= 1) {
        return ConsumptionIndicatorRingLayout(
            strokeWidth = preferredStrokeWidth,
            ringGap = preferredRingGap
        )
    }

    val availableStep = (outerRadius - minimumCenterTextRadius) / (ringCount - 1)
    val preferredStep = preferredStrokeWidth + preferredRingGap
    if (availableStep >= preferredStep) {
        return ConsumptionIndicatorRingLayout(
            strokeWidth = preferredStrokeWidth,
            ringGap = preferredRingGap
        )
    }

    val compressedGap = minimumRingGap.coerceAtMost(availableStep)
    val compressedStrokeWidth = (availableStep - compressedGap).coerceAtLeast(minimumStrokeWidth)
    return ConsumptionIndicatorRingLayout(
        strokeWidth = compressedStrokeWidth,
        ringGap = compressedGap
    )
}

private fun sanitizeConsumptionRatio(ratio: Float): Float {
    return if (ratio.isFinite()) ratio.coerceAtLeast(0f) else 0f
}

private fun DrawScope.drawConsumptionIndicatorRing(
    radius: Float,
    progress: Float,
    color: Color,
    trackColor: Color,
    strokeWidth: Float
) {
    drawCircle(
        color = trackColor,
        radius = radius,
        style = Stroke(width = strokeWidth)
    )

    val clampedProgress = progress.coerceIn(0f, 1f)
    if (clampedProgress >= 1f) {
        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )
    } else if (clampedProgress > 0f) {
        drawArc(
            color = color,
            startAngle = START_ANGLE_DEGREES,
            sweepAngle = 360f * clampedProgress,
            useCenter = false,
            topLeft = Offset(
                x = center.x - radius,
                y = center.y - radius
            ),
            size = Size(
                width = radius * 2f,
                height = radius * 2f
            ),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
