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
                // Required for BlendMode.Clear to punch a transparent hole only
                // through the elements drawn in this canvas, not through the whole screen.
                .graphicsLayer { alpha = 0.99f }
        ) {
            val preferredStrokeWidth = IndicatorStrokeWidth.toPx()
            val outerRadius = min(size.width, size.height) / 2f - preferredStrokeWidth / 2f
            if (outerRadius <= 0f) {
                return@Canvas
            }

            if (ratio <= 1f) {
                // Base cycle (0% - 100%)
                drawCircle(
                    color = trackColor,
                    radius = outerRadius,
                    style = Stroke(width = preferredStrokeWidth)
                )

                val clampedProgress = ratio.coerceIn(0f, 1f)
                if (clampedProgress >= 1f) {
                    drawCircle(
                        color = levelColor,
                        radius = outerRadius,
                        style = Stroke(width = preferredStrokeWidth)
                    )
                } else if (clampedProgress > 0f) {
                    drawArc(
                        color = levelColor,
                        startAngle = START_ANGLE_DEGREES,
                        sweepAngle = 360f * clampedProgress,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2f, outerRadius * 2f),
                        style = Stroke(width = preferredStrokeWidth, cap = StrokeCap.Round)
                    )
                }
            } else {
                // Overflow cycles (> 100%)
                // Draw the underlying completed cycle in the exact same solid levelColor
                drawCircle(
                    color = levelColor,
                    radius = outerRadius,
                    style = Stroke(width = preferredStrokeWidth)
                )

                val remainder = ratio - floor(ratio)
                val progressToDraw = if (remainder == 0f && ratio > 0f) 1f else remainder

                if (progressToDraw >= 1f) {
                    // Perfectly landed on a completed lap (e.g., 200%, 300%)
                    drawCircle(
                        color = levelColor,
                        radius = outerRadius,
                        style = Stroke(width = preferredStrokeWidth)
                    )
                } else if (progressToDraw > 0f) {
                    // Calculate the coordinates of the head (the moving end of the progress arc)
                    val endAngleDegrees = START_ANGLE_DEGREES + (360f * progressToDraw)
                    val endAngleRad = Math.toRadians(endAngleDegrees.toDouble())
                    val headX = center.x + outerRadius * cos(endAngleRad).toFloat()
                    val headY = center.y + outerRadius * sin(endAngleRad).toFloat()

                    // Punch a transparent circular cutout exactly at the head of the progress.
                    // The cutout is slightly larger than the stroke width to provide the visual padding.
                    drawCircle(
                        color = Color.Black, // Color is ignored by BlendMode.Clear
                        radius = (preferredStrokeWidth / 2f) + 4.dp.toPx(),
                        center = Offset(headX, headY),
                        blendMode = BlendMode.Clear
                    )

                    // Draw the active overflow progress.
                    // Since it's drawn after the cutout, its rounded cap sits over the trailing
                    // half of the hole we just punched, leaving a crisp crescent gap directly AHEAD of it.
                    drawArc(
                        color = levelColor,
                        startAngle = START_ANGLE_DEGREES,
                        sweepAngle = 360f * progressToDraw,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2f, outerRadius * 2f),
                        style = Stroke(width = preferredStrokeWidth, cap = StrokeCap.Round)
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
