package com.mgruchala.drinkwise.presentation.daydetails.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme

@Composable
fun ConsumptionCircle(
    consumedUnits: Float,
    limitUnits: Float,
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier
) {
    val color = when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }

    val progress = (consumedUnits / limitUnits).coerceIn(0f, 1f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.matchParentSize(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 12.dp
        )

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

private fun formatUnits(value: Float): String {
    return if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        value.toString()
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
            modifier = Modifier.size(200.dp)
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
            modifier = Modifier.size(200.dp)
        )
    }
}

@Preview(showBackground = true, name = "High consumption")
@Composable
private fun ConsumptionCircleHighPreview() {
    DrinkWiseTheme {
        ConsumptionCircle(
            consumedUnits = 9f,
            limitUnits = 7f,
            alcoholUnitLevel = AlcoholUnitLevel.High(9f, 7f),
            modifier = Modifier.size(200.dp)
        )
    }
}
