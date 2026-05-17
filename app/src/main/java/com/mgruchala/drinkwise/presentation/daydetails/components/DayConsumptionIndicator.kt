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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

private const val START_ANGLE_DEGREES = -90f
private val IndicatorStrokeWidth = 12.dp

internal fun calculateConsumptionIndicatorBaseProgress(ratio: Float): Float {
    return ratio.coerceIn(0f, 1f)
}

internal fun calculateConsumptionIndicatorOverflowProgress(ratio: Float): Float {
    if (ratio <= 1f) {
        return 0f
    }

    val currentLapProgress = ratio - floor(ratio)
    return if (currentLapProgress == 0f) 1f else currentLapProgress
}

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
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.99f }
        ) {
            val preferredStrokeWidth = IndicatorStrokeWidth.toPx()
            val outerRadius = min(size.width, size.height) / 2f - preferredStrokeWidth / 2f
            if (outerRadius <= 0f) {
                return@Canvas
            }

            if (ratio <= 1f) {
                drawCircle(
                    color = trackColor,
                    radius = outerRadius,
                    style = Stroke(width = preferredStrokeWidth)
                )

                drawConsumptionProgress(
                    color = levelColor,
                    radius = outerRadius,
                    strokeWidth = preferredStrokeWidth,
                    progress = calculateConsumptionIndicatorBaseProgress(ratio)
                )
            } else {
                drawCircle(
                    color = levelColor,
                    radius = outerRadius,
                    style = Stroke(width = preferredStrokeWidth)
                )

                val overflowProgress = calculateConsumptionIndicatorOverflowProgress(ratio)
                if (overflowProgress > 0f && overflowProgress < 1f) {
                    val endAngleDegrees = START_ANGLE_DEGREES + (360f * overflowProgress)
                    val endAngleRad = Math.toRadians(endAngleDegrees.toDouble())
                    val headX = center.x + outerRadius * cos(endAngleRad).toFloat()
                    val headY = center.y + outerRadius * sin(endAngleRad).toFloat()

                    drawCircle(
                        color = Color.Black,
                        radius = (preferredStrokeWidth / 2f) + 4.dp.toPx(),
                        center = Offset(headX, headY),
                        blendMode = BlendMode.Clear
                    )

                    drawConsumptionProgress(
                        color = levelColor,
                        radius = outerRadius,
                        strokeWidth = preferredStrokeWidth,
                        progress = overflowProgress
                    )
                }
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

private fun DrawScope.drawConsumptionProgress(
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
            startAngle = START_ANGLE_DEGREES,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
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
