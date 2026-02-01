package com.mgruchala.drinkwise.presentation.daydetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.daydetails.components.ConsumptionCircle
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkListItem
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DayDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")))
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
        DayDetailsContent(
            state = state,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun DayDetailsContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingContent(modifier)
        state.error != null -> ErrorContent(
            error = state.error,
            onRetry = { /* Retry handled by flow resubscription */ },
            modifier = modifier
        )
        state.drinks.isEmpty() -> EmptyContent(state, modifier)
        else -> DrinkListContent(state, modifier)
    }
}

@Composable
private fun DrinkListContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ConsumptionCircle(
                    consumedUnits = state.totalUnits,
                    limitUnits = state.dailyLimit,
                    alcoholUnitLevel = state.alcoholUnitLevel,
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        items(state.drinks, key = { it.uid }) { drink ->
            DrinkListItem(
                drink = drink,
                onClick = { /* Future: edit/delete */ }
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.day_details_retry))
        }
    }
}

@Composable
private fun EmptyContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ConsumptionCircle(
            consumedUnits = state.totalUnits,
            limitUnits = state.dailyLimit,
            alcoholUnitLevel = state.alcoholUnitLevel,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.day_details_no_drinks),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Loading state")
@Composable
private fun DayDetailsLoadingPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = DayDetailsState(isLoading = true)
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun DayDetailsEmptyPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = DayDetailsState(
                isLoading = false,
                drinks = emptyList(),
                totalUnits = 0f,
                dailyLimit = 7f,
                alcoholUnitLevel = AlcoholUnitLevel.Low(0f, 7f)
            )
        )
    }
}

@Preview(showBackground = true, name = "With drinks")
@Composable
private fun DayDetailsWithDrinksPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = DayDetailsState(
                isLoading = false,
                drinks = listOf(
                    DrinkEntity(uid = 1, quantity = 330, alcoholContent = 5.0f, timestamp = System.currentTimeMillis()),
                    DrinkEntity(uid = 2, quantity = 500, alcoholContent = 4.5f, timestamp = System.currentTimeMillis() - 3600000),
                    DrinkEntity(uid = 3, quantity = 1500, alcoholContent = 12.0f, timestamp = System.currentTimeMillis() - 7200000)
                ),
                totalUnits = 5.5f,
                dailyLimit = 7f,
                alcoholUnitLevel = AlcoholUnitLevel.Alarming(5.5f, 7f)
            )
        )
    }
}

@Preview(showBackground = true, name = "Error state")
@Composable
private fun DayDetailsErrorPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = DayDetailsState(
                isLoading = false,
                error = "Failed to load drinks"
            )
        )
    }
}
