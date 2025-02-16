package com.mgruchala.drinkwise.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.theme.DrinkWiseTheme

@Composable
fun HomeScreen() {
    HomeScreenContent()
}

@Composable
fun HomeScreenContent() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                DrinksSummaryCard(
                    modifier = Modifier.padding(16.dp),
                    title = "Today",
                    alcoholUnitCount = 2.5f,
                    alcoholUnitLimit = 4f
                )
                DrinksSummaryCard(
                    modifier = Modifier.padding(16.dp),
                    title = "This week",
                    alcoholUnitCount = 10f,
                    alcoholUnitLimit = 14f
                )
                DrinksSummaryCard(
                    modifier = Modifier.padding(16.dp),
                    title = "This month",
                    alcoholUnitCount = 30f,
                    alcoholUnitLimit = 40f
                )
            }
        }
    )
}

@Composable
fun DrinksSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    alcoholUnitCount: Float,
    alcoholUnitLimit: Float
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            Column {
                Text(title)
                Text("$alcoholUnitCount / $alcoholUnitLimit")
            }
        }
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun HomeScreenPreviewLightTheme() {
    DrinkWiseTheme{ HomeScreenContent() }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun HomeScreenPreviewDarkTheme() {
    DrinkWiseTheme(darkTheme = true) { HomeScreenContent() }
}
