package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow

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
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val centerColor = MaterialTheme.colorScheme.surface
    val centerStrokeColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface
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
            val diameter = size.minDimension * 0.76f
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val strokeWidth = size.minDimension * 0.08f

            if (isOverLimit) {
                drawCircle(
                    color = levelColor,
                    radius = size.minDimension * 0.38f,
                    center = center
                )
                drawCircle(
                    color = centerColor,
                    radius = size.minDimension * 0.23f,
                    center = center
                )
                drawCircle(
                    color = centerStrokeColor,
                    radius = size.minDimension * 0.23f,
                    center = center,
                    style = Stroke(width = size.minDimension * 0.012f)
                )
                val bubbleCenter = Offset(
                    x = center.x + size.minDimension * 0.29f,
                    y = center.y - size.minDimension * 0.29f
                )
                drawCircle(
                    color = levelColor,
                    radius = size.minDimension * 0.14f,
                    center = bubbleCenter
                )
                drawCircle(
                    color = centerColor,
                    radius = size.minDimension * 0.11f,
                    center = bubbleCenter
                )
            } else {
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = levelColor,
                    startAngle = -90f,
                    sweepAngle = 360f * ratio.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
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
