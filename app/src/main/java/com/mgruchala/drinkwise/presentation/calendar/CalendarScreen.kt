package com.mgruchala.drinkwise.presentation.calendar

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    CalendarScreenContent(
        state = state,
        onDeleteDrink = viewModel::deleteDrink
    )
}

@Composable
fun CalendarScreenContent(
    state: CalendarScreenState,
    onDeleteDrink: (Int) -> Unit = { _ -> }
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.drinks.isEmpty()) {
            Text(
                text = "No drinks recorded",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            DrinksList(
                drinks = state.drinks,
                onDeleteDrink = onDeleteDrink
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinksList(
    drinks: List<DrinkItem>,
    onDeleteDrink: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Your Drinks History",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(drinks, key = { it.id }) { drink ->
            var show by remember { mutableStateOf(true) }
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        show = false
                        true
                    } else {
                        false
                    }
                }
            )
            
            LaunchedEffect(show) {
                if (!show) {
                    delay(300) // Wait for animation to finish
                    onDeleteDrink(drink.id)
                }
            }
            
            AnimatedVisibility(
                visible = show,
                exit = shrinkHorizontally(animationSpec = tween(300)) + fadeOut()
            ) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        DismissBackground(dismissState)
                    },
                    content = {
                        DrinkCard(
                            drink = drink,
                            onDelete = {
                                show = false
                            }
                        )
                    },
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
fun DrinkCard(
    drink: DrinkItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = "Drink",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${drink.quantity}ml, ${drink.alcoholContent}% ABV",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete drink",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = drink.formattedDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Alcohol Units",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f", drink.alcoholUnits),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
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
fun CalendarScreenPreviewLightTheme() {
    DrinkWiseTheme {
        CalendarScreenContent(
            state = dummyCalendarScreenState
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
fun CalendarScreenPreviewDarkTheme() {
    DrinkWiseTheme(darkTheme = true) {
        CalendarScreenContent(
            state = dummyCalendarScreenState
        )
    }
}

val dummyCalendarScreenState = CalendarScreenState(
    drinks = listOf(
        DrinkItem(
            id = 1,
            quantity = 330,
            alcoholContent = 5.0f,
            alcoholUnits = 1.65,
            formattedDate = "Jan 01, 2023 18:30",
            timestamp = 1672596600000 // Jan 01, 2023 18:30
        ),
        DrinkItem(
            id = 2,
            quantity = 500,
            alcoholContent = 4.5f,
            alcoholUnits = 2.25,
            formattedDate = "Jan 02, 2023 20:15",
            timestamp = 1672689300000 // Jan 02, 2023 20:15
        ),
        DrinkItem(
            id = 3,
            quantity = 50,
            alcoholContent = 40.0f,
            alcoholUnits = 2.0,
            formattedDate = "Jan 03, 2023 22:45",
            timestamp = 1672783500000 // Jan 03, 2023 22:45
        )
    ),
    isLoading = false
)
