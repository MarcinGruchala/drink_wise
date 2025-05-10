package com.mgruchala.drinkwise.presentation.common

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow

@Composable
fun AlcoholUnitLevelProgressIndicator(
    modifier: Modifier,
    strokeWidth: Dp = 5.dp,
    alcoholUnitLevel: AlcoholUnitLevel
) {
    val color = when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }

    CircularProgressIndicator(
        progress = { alcoholUnitLevel.unitCount / alcoholUnitLevel.limit },
        modifier = modifier,
        color = color,
        trackColor = MaterialTheme.colorScheme.inverseSurface,
        strokeWidth = strokeWidth,
    )
}
