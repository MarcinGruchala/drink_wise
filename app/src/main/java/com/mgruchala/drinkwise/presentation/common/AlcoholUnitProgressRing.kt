package com.mgruchala.drinkwise.presentation.common

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val StartAngleDegrees = -90f
private const val MinimumAlcoholUnitIndicatorLimit = 0.1f
private const val DayDetailsIndicatorDiameter = 220f
private const val DayDetailsOverflowGapPadding = 4f
private const val AlcoholUnitProgressRingAnimationDurationMillis = 700
private const val AlcoholUnitProgressRingAnimationDurationPerRatioMillis = 800
private const val AlcoholUnitProgressRingAnimationStartDelayMillis = 350
private val AlcoholUnitProgressRingAnimationEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

// Extra clear radius as a fraction of indicator diameter; 4dp on the 220dp details ring.
const val AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction =
    DayDetailsOverflowGapPadding / DayDetailsIndicatorDiameter

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

internal fun resolveAlcoholUnitIndicatorDrawRatio(
    targetRatio: Float,
    animatedRatio: Float,
    animateProgress: Boolean
): Float {
    return if (animateProgress) animatedRatio else targetRatio
}

internal fun calculateAlcoholUnitIndicatorAnimationDurationMillis(
    animationDurationMillis: Int,
    animationDurationPerRatioMillis: Int,
    startRatio: Float,
    targetRatio: Float
): Int {
    val baseDurationMillis = animationDurationMillis.coerceAtLeast(0)
    val durationPerRatioMillis = animationDurationPerRatioMillis.coerceAtLeast(0)
    val ratioDistance = abs(targetRatio - startRatio)

    return baseDurationMillis + (durationPerRatioMillis * ratioDistance).roundToInt()
}

internal fun calculateAlcoholUnitIndicatorInitialAnimationDelayMillis(
    animationStartDelayMillis: Int,
    isInitialAnimation: Boolean
): Int {
    return if (isInitialAnimation) animationStartDelayMillis.coerceAtLeast(0) else 0
}

internal fun alcoholUnitLevelIndicatorColor(alcoholUnitLevel: AlcoholUnitLevel): Color {
    return when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }
}

/**
 * Draws the shared alcohol unit progress ring.
 *
 * Progress animation is opt-in. When [animateProgress] is enabled, the ring animates the
 * displayed ratio while text and semantics in parent components can continue showing the final
 * value. A ratio of `1f` means the configured limit; values above `1f` move through the same
 * over-limit renderer used by static rings, including the cleared overflow gap.
 *
 * On first composition, the animated ratio starts at zero and waits [animationStartDelayMillis]
 * before drawing to the target. Later target changes start immediately from the current displayed
 * ratio. The duration for each transition is:
 *
 * `animationDurationMillis + animationDurationPerRatioMillis * abs(targetRatio - currentRatio)`.
 *
 * Use [animationDurationMillis] as the minimum/base time, and
 * [animationDurationPerRatioMillis] to slow down larger sweeps without making small changes feel
 * sluggish. Negative timing values are clamped to zero.
 */
@Composable
internal fun AlcoholUnitProgressRing(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.inverseSurface,
    strokeWidth: Dp = 5.dp,
    overflowGapPaddingFraction: Float = AlcoholUnitIndicatorDefaultOverflowGapPaddingFraction,
    animateProgress: Boolean = false,
    animationDurationMillis: Int = AlcoholUnitProgressRingAnimationDurationMillis,
    animationDurationPerRatioMillis: Int = AlcoholUnitProgressRingAnimationDurationPerRatioMillis,
    animationStartDelayMillis: Int = AlcoholUnitProgressRingAnimationStartDelayMillis
) {
    val targetRatio = calculateAlcoholUnitIndicatorRatio(
        unitCount = alcoholUnitLevel.unitCount,
        limit = alcoholUnitLevel.limit
    )
    val animatedRatio = remember { Animatable(0f) }
    var isInitialAnimation by remember { mutableStateOf(true) }

    LaunchedEffect(
        animateProgress,
        targetRatio,
        animationDurationMillis,
        animationDurationPerRatioMillis,
        animationStartDelayMillis
    ) {
        if (animateProgress) {
            val initialDelayMillis = calculateAlcoholUnitIndicatorInitialAnimationDelayMillis(
                animationStartDelayMillis = animationStartDelayMillis,
                isInitialAnimation = isInitialAnimation
            )
            val durationMillis = calculateAlcoholUnitIndicatorAnimationDurationMillis(
                animationDurationMillis = animationDurationMillis,
                animationDurationPerRatioMillis = animationDurationPerRatioMillis,
                startRatio = animatedRatio.value,
                targetRatio = targetRatio
            )
            isInitialAnimation = false
            animatedRatio.animateTo(
                targetValue = targetRatio,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    delayMillis = initialDelayMillis,
                    easing = AlcoholUnitProgressRingAnimationEasing
                )
            )
        } else {
            isInitialAnimation = false
            animatedRatio.snapTo(targetRatio)
        }
    }

    val ratio = resolveAlcoholUnitIndicatorDrawRatio(
        targetRatio = targetRatio,
        animatedRatio = animatedRatio.value,
        animateProgress = animateProgress
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

@Preview(
    name = "Light",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun AlcoholUnitProgressRingPreviewLightTheme() {
    AlcoholUnitProgressRingPreview(darkTheme = false)
}

@Preview(
    name = "Dark",
    showBackground = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AlcoholUnitProgressRingPreviewDarkTheme() {
    AlcoholUnitProgressRingPreview(darkTheme = true)
}

@Composable
private fun AlcoholUnitProgressRingPreview(darkTheme: Boolean) {
    DrinkWiseTheme(darkTheme = darkTheme) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AlcoholUnitProgressRing(
                    alcoholUnitLevel = AlcoholUnitLevel.Low(1f, 4f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                AlcoholUnitProgressRing(
                    alcoholUnitLevel = AlcoholUnitLevel.Alarming(3.2f, 4f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                AlcoholUnitProgressRing(
                    alcoholUnitLevel = AlcoholUnitLevel.High(5.6f, 4f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                AlcoholUnitProgressRing(
                    alcoholUnitLevel = AlcoholUnitLevel.High(14.8f, 4f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}
