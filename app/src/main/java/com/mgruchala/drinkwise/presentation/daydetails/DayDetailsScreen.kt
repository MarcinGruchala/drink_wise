package com.mgruchala.drinkwise.presentation.daydetails

import android.content.res.Configuration
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mgruchala.alcohol_database.DrinkEntity
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.daydetails.components.ConsumptionCircle
import com.mgruchala.drinkwise.presentation.daydetails.components.DateFormatter
import com.mgruchala.drinkwise.presentation.daydetails.components.DrinkListItem
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val MAX_HISTORY_DAYS = 10000

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DayDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DayDetailsViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val state by viewModel.state.collectAsState()
    val initialDate = viewModel.getInitialDate()
    val today = LocalDate.now()

    // Calculate initial page: days from selected date to today
    val initialPage = ChronoUnit.DAYS.between(initialDate, today).toInt().coerceAtLeast(0)

    // Track the currently displayed date for the app bar
    var currentDate by remember { mutableStateOf(initialDate) }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { MAX_HISTORY_DAYS }
    )

    val view = LocalView.current
    val context = LocalContext.current

    // Update ViewModel and UI when page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val newDate = today.minusDays(page.toLong())
                if (newDate != currentDate) {
                    currentDate = newDate
                    viewModel.selectDate(newDate)
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Determine title based on collapse state
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    val title = if (collapsedFraction > 0.5f) {
        DateFormatter.formatCollapsedDate(currentDate)
    } else {
        DateFormatter.formatFullDate(currentDate)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column(
                        modifier = Modifier.semantics {
                            heading()
                            liveRegion = LiveRegionMode.Polite
                        }
                    ) {
                        Text(title)
                        if (collapsedFraction < 0.5f) {
                            Text(
                                text = DateFormatter.formatRelativeTime(currentDate, context),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.day_details_navigate_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            userScrollEnabled = true,
            beyondViewportPageCount = 1
        ) { page ->
            val pageDate = today.minusDays(page.toLong())
            val isCurrentPage = page == pagerState.currentPage

            // Show actual content only for the current page
            // Adjacent pages show loading state until swiped to
            if (isCurrentPage && state.selectedDate == pageDate) {
                DayDetailsContent(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            } else if (isCurrentPage) {
                // Loading state while data is being fetched
                LoadingContent(modifier = Modifier.fillMaxSize())
            } else {
                // Placeholder for adjacent pages
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DayDetailsContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    when {
        state.isLoading -> LoadingContent(modifier)
        state.error != null -> ErrorContent(
            error = state.error,
            onRetry = { /* Retry handled by flow resubscription */ },
            modifier = modifier
        )
        state.drinks.isEmpty() -> EmptyContent(
            state = state,
            modifier = modifier,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
        else -> DrinkListContent(
            state = state,
            modifier = modifier,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DrinkListContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeDrinkListContent(
            state = state,
            modifier = modifier,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    } else {
        PortraitDrinkListContent(
            state = state,
            modifier = modifier,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PortraitDrinkListContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val listState = rememberLazyListState()

    // Create shared element key based on date
    val sharedElementKey = "day_indicator_${state.selectedDate.toEpochDay()}"

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val circleModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier
                            .size(200.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = sharedElementKey),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    }
                } else {
                    Modifier.size(200.dp)
                }

                ConsumptionCircle(
                    consumedUnits = state.totalUnits,
                    limitUnits = state.dailyLimit,
                    alcoholUnitLevel = state.alcoholUnitLevel,
                    modifier = circleModifier
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun LandscapeDrinkListContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val listState = rememberLazyListState()

    // Create shared element key based on date
    val sharedElementKey = "day_indicator_${state.selectedDate.toEpochDay()}"

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left side: Consumption circle
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val circleModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier
                        .size(180.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = sharedElementKey),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                }
            } else {
                Modifier.size(180.dp)
            }

            ConsumptionCircle(
                consumedUnits = state.totalUnits,
                limitUnits = state.dailyLimit,
                alcoholUnitLevel = state.alcoholUnitLevel,
                modifier = circleModifier
            )
        }

        // Right side: Drink list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.drinks, key = { it.uid }) { drink ->
                DrinkListItem(
                    drink = drink,
                    onClick = { /* Future: edit/delete */ }
                )
            }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EmptyContent(
    state: DayDetailsState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Create shared element key based on date
    val sharedElementKey = "day_indicator_${state.selectedDate.toEpochDay()}"

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val circleModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier
                        .size(180.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = sharedElementKey),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                }
            } else {
                Modifier.size(180.dp)
            }

            ConsumptionCircle(
                consumedUnits = state.totalUnits,
                limitUnits = state.dailyLimit,
                alcoholUnitLevel = state.alcoholUnitLevel,
                modifier = circleModifier
            )
            Spacer(modifier = Modifier.width(32.dp))
            Text(
                text = stringResource(R.string.day_details_no_drinks),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val circleModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier
                        .size(200.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = sharedElementKey),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                }
            } else {
                Modifier.size(200.dp)
            }

            ConsumptionCircle(
                consumedUnits = state.totalUnits,
                limitUnits = state.dailyLimit,
                alcoholUnitLevel = state.alcoholUnitLevel,
                modifier = circleModifier
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.day_details_no_drinks),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, name = "Loading state")
@Composable
private fun DayDetailsLoadingPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = DayDetailsState(isLoading = true)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, name = "With drinks - Portrait")
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(
    showBackground = true,
    name = "With drinks - Landscape",
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 800,
    heightDp = 400
)
@Composable
private fun DayDetailsWithDrinksLandscapePreview() {
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

@OptIn(ExperimentalSharedTransitionApi::class)
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, name = "Over limit - Multiple rings")
@Composable
private fun DayDetailsOverLimitPreview() {
    DrinkWiseTheme {
        DayDetailsContent(
            state = DayDetailsState(
                isLoading = false,
                drinks = listOf(
                    DrinkEntity(uid = 1, quantity = 500, alcoholContent = 12.0f, timestamp = System.currentTimeMillis()),
                    DrinkEntity(uid = 2, quantity = 500, alcoholContent = 12.0f, timestamp = System.currentTimeMillis() - 3600000)
                ),
                totalUnits = 12f,
                dailyLimit = 7f,
                alcoholUnitLevel = AlcoholUnitLevel.High(12f, 7f)
            )
        )
    }
}
