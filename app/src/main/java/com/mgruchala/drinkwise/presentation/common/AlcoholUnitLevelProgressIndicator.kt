package com.mgruchala.drinkwise.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel

@Composable
fun AlcoholUnitLevelProgressIndicator(
    modifier: Modifier,
    strokeWidth: Dp = 5.dp,
    alcoholUnitLevel: AlcoholUnitLevel
) {
    AlcoholUnitProgressRing(
        alcoholUnitLevel = alcoholUnitLevel,
        trackColor = MaterialTheme.colorScheme.inverseSurface,
        strokeWidth = strokeWidth,
        modifier = modifier
    )
}
