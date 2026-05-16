package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import kotlin.math.ceil

private const val OVERFLOW_RING_PADDING_DP = 14

@Composable
fun DayConsumptionIndicator(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier
) {
    val consumed = alcoholUnitLevel.unitCount
    val limit = alcoholUnitLevel.limit.coerceAtLeast(0.1f)
    val ratio = consumed / limit
    val ringCount = ceil(ratio).toInt().coerceAtLeast(1)
    val ringProgresses = calculateRingProgresses(ratio, ringCount)
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
        ringProgresses.forEachIndexed { index, progress ->
            val reversedIndex = ringProgresses.lastIndex - index
            val ringPadding = (reversedIndex * OVERFLOW_RING_PADDING_DP).dp
            val ringColor = if (index == 0) {
                levelColor
            } else {
                AlcoholUnitLevelHigh
            }
            val trackColor = if (index == 0) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
            }

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ringPadding),
                color = ringColor,
                trackColor = trackColor,
                strokeWidth = strokeWidthForRing(reversedIndex)
            )
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

private fun calculateRingProgresses(ratio: Float, ringCount: Int): List<Float> {
    return (0 until ringCount).map { ringIndex ->
        val ringStart = ringIndex.toFloat()
        val ringEnd = ringStart + 1f

        when {
            ratio >= ringEnd -> 1f
            ratio > ringStart -> ratio - ringStart
            else -> 0f
        }
    }
}

private fun strokeWidthForRing(reversedIndex: Int): Dp {
    return when (reversedIndex) {
        0 -> 12.dp
        1 -> 10.dp
        else -> 8.dp
    }
}
