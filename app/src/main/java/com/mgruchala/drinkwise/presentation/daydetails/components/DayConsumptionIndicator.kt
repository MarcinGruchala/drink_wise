package com.mgruchala.drinkwise.presentation.daydetails.components

import android.content.res.Configuration
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
import com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRing
import com.mgruchala.drinkwise.presentation.common.calculateAlcoholUnitIndicatorRatio
import com.mgruchala.drinkwise.presentation.common.calculateAlcoholUnitIndicatorSafeLimit
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme

private val IndicatorStrokeWidth = 12.dp

@Composable
fun DayConsumptionIndicator(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier
) {
    val consumed = alcoholUnitLevel.unitCount
    val limit = calculateAlcoholUnitIndicatorSafeLimit(alcoholUnitLevel.limit)
    val ratio = calculateAlcoholUnitIndicatorRatio(
        unitCount = consumed,
        limit = alcoholUnitLevel.limit
    )
    val percent = (ratio * 100f).toInt()
    val consumedText = formatDayDetailsUnits(consumed)
    val limitText = formatDayDetailsUnits(limit)
    val overLimitAmount = (consumed - limit).coerceAtLeast(0f)
    val overLimitText = formatDayDetailsUnits(overLimitAmount)
    val isOverLimit = ratio > 1f

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
        AlcoholUnitProgressRing(
            alcoholUnitLevel = alcoholUnitLevel,
            trackColor = trackColor,
            strokeWidth = IndicatorStrokeWidth,
            modifier = Modifier
                .fillMaxSize(),
            animateProgress = true,
            animationStartDelayMillis = 1000
        )

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
