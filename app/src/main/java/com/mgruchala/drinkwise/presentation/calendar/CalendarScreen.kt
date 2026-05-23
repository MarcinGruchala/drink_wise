package com.mgruchala.drinkwise.presentation.calendar

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.common.AlcoholUnitProgressRing
import com.mgruchala.drinkwise.presentation.common.calculateAlcoholUnitIndicatorRatio
import com.mgruchala.drinkwise.presentation.common.calculateAlcoholUnitIndicatorSafeLimit
import com.mgruchala.drinkwise.presentation.common.formatAlcoholUnitsCompact
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val MonthConsumptionIndicatorSize = 184.dp
private val MonthConsumptionIndicatorTopGap = 24.dp
private val MonthConsumptionIndicatorBottomGap = 32.dp
private val MonthConsumptionIndicatorStrokeWidth = 10.dp
private val PreviewToday: LocalDate = LocalDate.of(2026, 5, 21)

@Composable
fun CalendarScreen(
    onDayClick: (LocalDate) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        CalendarScreenContent(
            calendarData = state.calendarData,
            monthlyAlcoholUnitLevels = state.monthlyAlcoholUnitLevels,
            today = state.today,
            onDayClick = onDayClick
        )
    }
}

@Composable
fun CalendarScreenContent(
    calendarData: Map<YearMonth, List<CalendarDayData>>,
    monthlyAlcoholUnitLevels: Map<YearMonth, AlcoholUnitLevel> = emptyMap(),
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val sortedMonths = calendarData.keys.sortedDescending()
    val initialPage = 0
    val pagerState = rememberPagerState(initialPage = initialPage) { sortedMonths.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            if (sortedMonths.isNotEmpty()) {
                MonthNavigationHeader(
                    currentMonth = sortedMonths[pagerState.currentPage],
                    canGoForward = pagerState.currentPage > 0,
                    canGoBack = pagerState.currentPage < sortedMonths.size - 1,
                    onNextMonth = {
                        coroutineScope.launch {
                            if (pagerState.currentPage > 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onPreviousMonth = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < sortedMonths.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val month = sortedMonths[page]
                val days = calendarData[month] ?: emptyList()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    monthlyAlcoholUnitLevels[month]?.let { monthAlcoholUnitLevel ->
                        Spacer(modifier = Modifier.height(MonthConsumptionIndicatorTopGap))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            MonthConsumptionIndicator(alcoholUnitLevel = monthAlcoholUnitLevel)
                        }
                        Spacer(modifier = Modifier.height(MonthConsumptionIndicatorBottomGap))
                    }

                    DayOfWeekHeader()
                    Spacer(modifier = Modifier.height(8.dp))

                    MonthCalendar(
                        month = month,
                        originalDays = days,
                        today = today,
                        onDayClick = onDayClick
                    )
                }
            }
        }
    }
}

@Composable
fun MonthNavigationHeader(
    currentMonth: YearMonth,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNextMonth,
            enabled = canGoForward
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.calendar_next_month)
            )
        }

        Text(
            text = currentMonth.format(formatter)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(
            onClick = onPreviousMonth,
            enabled = canGoBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.calendar_previous_month)
            )
        }

    }
}

@Composable
fun MonthCalendar(
    month: YearMonth,
    originalDays: List<CalendarDayData>,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val daysMap = originalDays.associateBy { it.date.dayOfMonth }

    val allDaysOfMonth = (1..month.lengthOfMonth()).map { dayOfMonth ->
        daysMap[dayOfMonth] ?: CalendarDayData(
            date = month.atDay(dayOfMonth),
            alcoholUnitLevel = null
        )
    }
    val firstDayOfMonth = month.atDay(1)
    val startPaddingCells = firstDayOfMonth.dayOfWeek.value - 1

    val allCells = mutableListOf<CalendarDayData?>()
    repeat(startPaddingCells) { allCells.add(null) }
    allCells.addAll(allDaysOfMonth)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        allCells.chunked(7).forEach { weekCells ->
            WeekRow(
                weekDays = weekCells,
                today = today,
                onDayClick = onDayClick
            )
        }
    }
}

@Composable
fun DayOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val days = listOf(
            stringResource(id = R.string.calendar_day_mon),
            stringResource(id = R.string.calendar_day_tue),
            stringResource(id = R.string.calendar_day_wed),
            stringResource(id = R.string.calendar_day_thu),
            stringResource(id = R.string.calendar_day_fri),
            stringResource(id = R.string.calendar_day_sat),
            stringResource(id = R.string.calendar_day_sun)
        )
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeekRow(
    weekDays: List<CalendarDayData?>,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(7) { index ->
            val dayData = if (index < weekDays.size) weekDays[index] else null
            Box(modifier = Modifier.weight(1f)) {
                if (dayData != null) {
                    DayCell(
                        dayData = dayData,
                        today = today,
                        onClick = { onDayClick(dayData.date) }
                    )
                } else {
                    EmptyDayPlaceholder()
                }
            }
        }
    }
}

@Composable
fun MonthConsumptionIndicator(
    alcoholUnitLevel: AlcoholUnitLevel,
    modifier: Modifier = Modifier
) {
    val consumed = alcoholUnitLevel.unitCount
    val limit = calculateAlcoholUnitIndicatorSafeLimit(alcoholUnitLevel.limit)
    val ratio = calculateAlcoholUnitIndicatorRatio(
        unitCount = consumed,
        limit = alcoholUnitLevel.limit
    )
    val percent = (ratio * 100f).toInt()
    val consumedText = formatAlcoholUnitsCompact(consumed)
    val limitText = formatAlcoholUnitsCompact(limit)
    val overLimitAmount = (consumed - limit).coerceAtLeast(0f)
    val overLimitText = formatAlcoholUnitsCompact(overLimitAmount)
    val isOverLimit = ratio > 1f
    val contentDescription = if (isOverLimit) {
        stringResource(
            id = R.string.calendar_month_consumption_indicator_over_limit_description,
            consumedText,
            limitText,
            percent.toString(),
            overLimitText
        )
    } else {
        stringResource(
            id = R.string.calendar_month_consumption_indicator_description,
            consumedText,
            limitText,
            percent.toString()
        )
    }

    Box(
        modifier = modifier
            .size(MonthConsumptionIndicatorSize)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        AlcoholUnitProgressRing(
            alcoholUnitLevel = alcoholUnitLevel,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = MonthConsumptionIndicatorStrokeWidth,
            modifier = Modifier.fillMaxSize(),
            animateProgress = true
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.calendar_month_consumption_percent, percent),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(
                    id = R.string.calendar_month_consumed_of_limit,
                    consumedText,
                    limitText
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.day_details_units_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (isOverLimit) {
            Text(
                text = stringResource(R.string.day_details_over_limit_amount, overLimitText),
                modifier = Modifier.align(Alignment.TopEnd),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyDayPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}

@Composable
fun DayCell(
    dayData: CalendarDayData,
    today: LocalDate,
    onClick: () -> Unit = {}
) {
    val isToday = dayData.date.isEqual(today)
    val dayDescriptionFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    val dayContentDescription = stringResource(
        id = R.string.calendar_day_content_description,
        dayData.date.format(dayDescriptionFormatter)
    )
    val backgroundModifier = if (isToday) {
        Modifier.background(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = dayContentDescription
                role = Role.Button
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp)
                .then(backgroundModifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayData.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )

            dayData.alcoholUnitLevel?.let {
                AlcoholUnitProgressRing(
                    modifier = Modifier.matchParentSize(),
                    alcoholUnitLevel = it,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun CalendarScreenPreviewLightTheme() {
    DrinkWiseTheme {
        val previewData = createPreviewCalendarData()
        CalendarScreenContent(
            calendarData = previewData,
            monthlyAlcoholUnitLevels = createPreviewMonthlyAlcoholUnitLevels(previewData),
            today = PreviewToday
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun CalendarScreenPreviewDarkTheme() {
    DrinkWiseTheme(darkTheme = true) {
        val previewData = createPreviewCalendarData()
        CalendarScreenContent(
            calendarData = previewData,
            monthlyAlcoholUnitLevels = createPreviewMonthlyAlcoholUnitLevels(previewData),
            today = PreviewToday
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(
    name = "Compact phone",
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun CalendarScreenPreviewCompactPhoneDarkTheme() {
    DrinkWiseTheme(darkTheme = true) {
        val previewData = createPreviewCalendarData()
        CalendarScreenContent(
            calendarData = previewData,
            monthlyAlcoholUnitLevels = createPreviewMonthlyAlcoholUnitLevels(previewData),
            today = PreviewToday
        )
    }
}

private fun createPreviewCalendarData(): Map<YearMonth, List<CalendarDayData>> {
    val result = mutableMapOf<YearMonth, List<CalendarDayData>>()

    for (i in 4 downTo 0) {
        val month = YearMonth.from(PreviewToday).minusMonths(i.toLong())
        val days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            CalendarDayData(date, AlcoholUnitLevel.fromUnitCount(1f, limit = 3f))
        }
        result[month] = days
    }

    return result.toSortedMap(compareByDescending { it })
}

private fun createPreviewMonthlyAlcoholUnitLevels(
    calendarData: Map<YearMonth, List<CalendarDayData>>
): Map<YearMonth, AlcoholUnitLevel> {
    return calendarData.mapValues { (_, days) ->
        val unitCount = days.sumOf { day ->
            day.alcoholUnitLevel?.unitCount?.toDouble() ?: 0.0
        }.toFloat()
        AlcoholUnitLevel.fromUnitCount(unitCount = unitCount, limit = 30f)
    }.toSortedMap(compareByDescending { it })
}
