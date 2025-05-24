package com.mgruchala.drinkwise.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.common.AlcoholUnitLevelProgressIndicator
import com.mgruchala.user_preferences.summary_period.CalculationMode

enum class DrinkSummaryCardPeriod {
    DAILY,
    WEEKLY,
    MONTHLY;

    fun getDisplayTitle(): Int {
        return when (this) {
            DAILY -> R.string.drinks_summary_card_today
            WEEKLY -> R.string.drinks_summary_card_this_week
            MONTHLY -> R.string.drinks_summary_card_this_month
        }
    }
}


@Composable
fun DrinksSummaryCard(
    modifier: Modifier = Modifier,
    period: DrinkSummaryCardPeriod,
    alcoholUnitLevel: AlcoholUnitLevel,
    forceExpanded: Boolean = false,
    currentMode: CalculationMode,
    onModeChange: (CalculationMode) -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(forceExpanded) }
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(bottom = if (expanded) 8.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(id = period.getDisplayTitle()),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        "${alcoholUnitLevel.unitCount} / ${alcoholUnitLevel.limit}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                AlcoholUnitLevelProgressIndicator(
                    modifier = Modifier.size(54.dp),
                    alcoholUnitLevel = alcoholUnitLevel,
                )
                IconButton(
                    onClick = { expanded = !expanded },
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) stringResource(id = R.string.drinks_summary_card_collapse_icon_content_description) else stringResource(
                            id = R.string.drinks_summary_card_expand_icon_content_description
                        )
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                val (option1Text, option2Text) = when (period) {
                    DrinkSummaryCardPeriod.DAILY -> stringResource(id = R.string.drinks_summary_card_daily_option_since_midnight) to stringResource(
                        id = R.string.drinks_summary_card_daily_option_last_24h
                    )

                    DrinkSummaryCardPeriod.WEEKLY -> stringResource(id = R.string.drinks_summary_card_weekly_option_this_week) to stringResource(
                        id = R.string.drinks_summary_card_weekly_option_last_7_days
                    )

                    DrinkSummaryCardPeriod.MONTHLY -> stringResource(id = R.string.drinks_summary_card_monthly_option_this_month) to stringResource(
                        id = R.string.drinks_summary_card_monthly_option_last_30_days
                    )
                }
                val options = listOf(option1Text, option2Text)

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    options.forEachIndexed { index, label ->
                        val modeForOption =
                            if (index == 0) CalculationMode.SINCE_START_OF_PERIOD else CalculationMode.ROLLING_PERIOD
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                onModeChange(modeForOption)
                            },
                            selected = currentMode == modeForOption
                        ) {
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrinksSummaryCardLowPreview() {
    DrinksSummaryCard(
        period = DrinkSummaryCardPeriod.DAILY,
        alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(1f, 4f),
        forceExpanded = false,
        currentMode = CalculationMode.ROLLING_PERIOD,
    )
}

@Preview(showBackground = true)
@Composable
fun DrinksSummaryCardAlarmingPreview() {
    DrinksSummaryCard(
        period = DrinkSummaryCardPeriod.DAILY,
        alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(3f, 4f),
        forceExpanded = false,
        currentMode = CalculationMode.ROLLING_PERIOD,
    )
}

@Preview(showBackground = true)
@Composable
fun DrinksSummaryCardHighPreview() {
    DrinksSummaryCard(
        period = DrinkSummaryCardPeriod.DAILY,
        alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(6f, 4f),
        forceExpanded = false,
        currentMode = CalculationMode.ROLLING_PERIOD,
    )
}

@Preview(showBackground = true)
@Composable
fun DrinksSummaryCardDailyExpandedPreview() {
    DrinksSummaryCard(
        period = DrinkSummaryCardPeriod.DAILY,
        alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(6f, 4f),
        forceExpanded = true,
        currentMode = CalculationMode.ROLLING_PERIOD,
    )
}

@Preview(showBackground = true)
@Composable
fun DrinksSummaryCardWeeklyExpandedPreview() {
    DrinksSummaryCard(
        period = DrinkSummaryCardPeriod.WEEKLY,
        alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(6f, 4f),
        forceExpanded = true,
        currentMode = CalculationMode.SINCE_START_OF_PERIOD,
    )
}

@Preview(showBackground = true)
@Composable
fun DrinksSummaryCardMonthlyExpandedPreview() {
    DrinksSummaryCard(
        period = DrinkSummaryCardPeriod.MONTHLY,
        alcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(6f, 4f),
        forceExpanded = true,
        currentMode = CalculationMode.ROLLING_PERIOD,
    )
}
