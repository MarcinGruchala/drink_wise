package com.mgruchala.drinkwise.presentation.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelAlarming
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelHigh
import com.mgruchala.drinkwise.presentation.theme.AlcoholUnitLevelLow
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    HomeScreenContent(
        state = state
    )
}

@Composable
fun HomeScreenContent(
    state: HomeScreenState
) {
    val openAddDrinkDialog = rememberSaveable { mutableStateOf(false) }

    if (openAddDrinkDialog.value) {
        AddDrinkDialog(
            onAddClick = {
                openAddDrinkDialog.value = false
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
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DrinksSummaryCard(
                    title = "Today",
                    alcoholUnitLevel = state.todayAlcoholUnitLevel
                )
                DrinksSummaryCard(
                    title = "This week",
                    alcoholUnitLevel = state.weekAlcoholUnitLevel
                )
                DrinksSummaryCard(
                    title = "This month",
                    alcoholUnitLevel = state.monthAlcoholUnitLevel
                )
            }
        }
    )
}

@Composable
fun DrinksSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    alcoholUnitLevel: AlcoholUnitLevel
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    "${alcoholUnitLevel.unitCount} / ${alcoholUnitLevel.limit}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            DrinkSummaryCardCircularProgressIndicator(
                alcoholUnitLevel = alcoholUnitLevel
            )
        }
    }
}

@Composable
fun DrinkSummaryCardCircularProgressIndicator(
    alcoholUnitLevel: AlcoholUnitLevel
) {
    val color = when (alcoholUnitLevel) {
        is AlcoholUnitLevel.Low -> AlcoholUnitLevelLow
        is AlcoholUnitLevel.Alarming -> AlcoholUnitLevelAlarming
        is AlcoholUnitLevel.High -> AlcoholUnitLevelHigh
    }

    CircularProgressIndicator(
        progress = { alcoholUnitLevel.unitCount / alcoholUnitLevel.limit },
        modifier = Modifier.size(54.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.inverseSurface,
        strokeWidth = 5.dp,
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
