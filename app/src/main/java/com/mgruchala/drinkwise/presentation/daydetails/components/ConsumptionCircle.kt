package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlin.math.ceil

private const val RING_PADDING_DP = 14

@Composable
fun ConsumptionCircle(
    consumedUnits: Float,
    limitUnits: Float,
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val context = LocalContext.current

    val baseColor = when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }

    // Calculate number of rings needed
    val ratio = if (limitUnits > 0) consumedUnits / limitUnits else 0f
    val numberOfRings = ceil(ratio).toInt().coerceAtLeast(1)
    val isOverLimit = ratio > 1f

    // Calculate progress for each ring
    val ringProgresses = calculateRingProgresses(ratio, numberOfRings)

    // Animated progress values for each ring
    val animatedProgresses = ringProgresses.map { targetProgress ->
        val animatable = remember(targetProgress) { Animatable(0f) }
        LaunchedEffect(targetProgress) {
            if (animate) {
                animatable.animateTo(
                    targetValue = targetProgress,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            } else {
                animatable.snapTo(targetProgress)
            }
        }
        animatable
    }

    // Accessibility description
    val percentOfLimit = ((consumedUnits / limitUnits) * 100).toInt()
    val accessibilityDescription = buildString {
        append(
            context.getString(
                R.string.day_details_a11y_indicator,
                formatUnits(consumedUnits),
                formatUnits(limitUnits),
                percentOfLimit
            )
        )
        if (isOverLimit) {
            append(context.getString(R.string.day_details_a11y_over_limit))
        }
    }

    Box(
        modifier = modifier.semantics {
            contentDescription = accessibilityDescription
        },
        contentAlignment = Alignment.Center
    ) {
        // Draw rings from outermost to innermost
        animatedProgresses.forEachIndexed { index, animatable ->
            val ringPadding = (index * RING_PADDING_DP).dp
            val ringColor = if (index > 0) AlcoholUnitLevelHigh else baseColor
            val strokeWidth = calculateStrokeWidth(index)

            CircularProgressIndicator(
                progress = { animatable.value },
                modifier = Modifier
                    .matchParentSize()
                    .padding(ringPadding),
                color = ringColor,
                trackColor = if (index == 0) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                strokeWidth = strokeWidth
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${formatUnits(consumedUnits)} / ${formatUnits(limitUnits)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.day_details_units_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Calculate progress values for each ring.
 * - First ring (outermost): Shows progress from 0% to 100%
 * - Additional rings: Show overflow progress
 */
private fun calculateRingProgresses(ratio: Float, numberOfRings: Int): List<Float> {
    if (numberOfRings == 1) {
        return listOf(ratio.coerceIn(0f, 1f))
    }

    return (0 until numberOfRings).map { ringIndex ->
        val ringStart = ringIndex.toFloat()
        val ringEnd = ringStart + 1f

        when {
            ratio >= ringEnd -> 1f // Ring is completely filled
            ratio > ringStart -> ratio - ringStart // Partially filled
            else -> 0f // Not reached yet
        }
    }
}

/**
 * Calculate stroke width for each ring.
 * Inner rings are slightly thinner.
 */
private fun calculateStrokeWidth(ringIndex: Int): Dp {
    return when (ringIndex) {
        0 -> 12.dp
        1 -> 10.dp
        else -> 8.dp
    }
}

private fun formatUnits(value: Float): String {
    return if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        "%.1f".format(value)
    }
}

@Preview(showBackground = true, name = "Low consumption")
@Composable
private fun ConsumptionCircleLowPreview() {
    DrinkWiseTheme {
        ConsumptionCircle(
            consumedUnits = 2f,
            limitUnits = 7f,
            alcoholUnitLevel = AlcoholUnitLevel.Low(2f, 7f),
            modifier = Modifier.size(200.dp),
            animate = false
        )
    }
}

@Preview(showBackground = true, name = "Alarming consumption")
@Composable
private fun ConsumptionCircleAlarmingPreview() {
    DrinkWiseTheme {
        ConsumptionCircle(
            consumedUnits = 5.5f,
            limitUnits = 7f,
            alcoholUnitLevel = AlcoholUnitLevel.Alarming(5.5f, 7f),
            modifier = Modifier.size(200.dp),
            animate = false
        )
    }
}

@Preview(showBackground = true, name = "High consumption - at limit")
@Composable
private fun ConsumptionCircleHighPreview() {
    DrinkWiseTheme {
        ConsumptionCircle(
            consumedUnits = 7f,
            limitUnits = 7f,
            alcoholUnitLevel = AlcoholUnitLevel.High(7f, 7f),
            modifier = Modifier.size(200.dp),
            animate = false
        )
    }
}

@Preview(showBackground = true, name = "Over limit - 150%")
@Composable
private fun ConsumptionCircleOverLimitPreview() {
    DrinkWiseTheme {
        ConsumptionCircle(
            consumedUnits = 10.5f,
            limitUnits = 7f,
            alcoholUnitLevel = AlcoholUnitLevel.High(10.5f, 7f),
            modifier = Modifier.size(200.dp),
            animate = false
        )
    }
}

@Preview(showBackground = true, name = "Over limit - 200%+")
@Composable
private fun ConsumptionCircleDoubleOverLimitPreview() {
    DrinkWiseTheme {
        ConsumptionCircle(
            consumedUnits = 16f,
            limitUnits = 7f,
            alcoholUnitLevel = AlcoholUnitLevel.High(16f, 7f),
            modifier = Modifier.size(200.dp),
            animate = false
        )
    }
}
