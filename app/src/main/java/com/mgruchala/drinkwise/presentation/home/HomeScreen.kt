package com.mgruchala.drinkwise.presentation.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import com.mgruchala.user_preferences.summary_period.CalculationMode

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    HomeScreenContent(
        state = state,
        registerNewDrinks = viewModel::registerNewDrinks
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreenContent(
    state: HomeScreenState,
    registerNewDrinks: (Int, Float, Int) -> Unit = { _, _, _ -> }
) {
    val openAddDrinkDialog = rememberSaveable { mutableStateOf(false) }

    if (openAddDrinkDialog.value) {
        AddDrinkDialog(
            onAddClick = { quantity, abv, amount ->
                registerNewDrinks(quantity, abv, amount)
            },
            onDismiss = {
                openAddDrinkDialog.value = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    openAddDrinkDialog.value = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DrinksSummaryCard(
                    period = DrinkSummaryCardPeriod.DAILY,
                    alcoholUnitLevel = state.todayAlcoholUnitLevel,
                    currentMode = CalculationMode.SINCE_START_OF_PERIOD,
                )
                DrinksSummaryCard(
                    period = DrinkSummaryCardPeriod.WEEKLY,
                    alcoholUnitLevel = state.weekAlcoholUnitLevel,
                    currentMode = CalculationMode.SINCE_START_OF_PERIOD,
                )
                DrinksSummaryCard(
                    period = DrinkSummaryCardPeriod.MONTHLY,
                    alcoholUnitLevel = state.monthAlcoholUnitLevel,
                    currentMode = CalculationMode.SINCE_START_OF_PERIOD,
                )
            }
        }
    )
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun HomeScreenPreviewLightTheme() {
    DrinkWiseTheme {
        HomeScreenContent(
            state = dummyState
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun HomeScreenPreviewDarkTheme() {
    DrinkWiseTheme(darkTheme = true) {
        HomeScreenContent(
            state = dummyState
        )
    }
}

val dummyState = HomeScreenState(
    todayAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(6f, 4f),
    weekAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(12f, 14f),
    monthAlcoholUnitLevel = AlcoholUnitLevel.fromUnitCount(15f, 30f),
)
