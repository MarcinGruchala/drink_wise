package com.mgruchala.drinkwise.presentation.daydetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.daydetails.components.DayConsumptionIndicator
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkListItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sampleDayDetailsDrinks = listOf(
    DrinkEntity(uid = 1, quantity = 500, alcoholContent = 5.2f, timestamp = 1_779_020_400_000L),
    DrinkEntity(uid = 2, quantity = 150, alcoholContent = 13.0f, timestamp = 1_779_016_800_000L),
    DrinkEntity(uid = 3, quantity = 330, alcoholContent = 4.8f, timestamp = 1_779_013_200_000L)
)

private val sampleDayDetailsLevel = AlcoholUnitLevel.fromUnitCount(
    unitCount = 5.6f,
    limit = 4f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(
    selectedDate: LocalDate,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = selectedDate.format(formatter))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.day_details_navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DayConsumptionIndicator(alcoholUnitLevel = sampleDayDetailsLevel)
                }
            }
            item {
                Text(
                    text = stringResource(R.string.day_details_drinks_section_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(sampleDayDetailsDrinks, key = { it.uid }) { drink ->
                DrinkListItem(drink = drink)
            }
        }
    }
}
