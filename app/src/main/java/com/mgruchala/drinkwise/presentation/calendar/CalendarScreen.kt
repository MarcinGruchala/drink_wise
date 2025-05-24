package com.mgruchala.drinkwise.presentation.calendar

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mgruchala.drinkwise.R
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.common.AlcoholUnitLevelProgressIndicator
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        CalendarScreenContent(calendarData = state.calendarData)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CalendarScreenContent(calendarData: Map<YearMonth, List<CalendarDayData>>) {
    val sortedMonths = calendarData.keys.sortedDescending()
    val initialPage = 0
    val pagerState = rememberPagerState(initialPage = initialPage) { sortedMonths.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
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

            Spacer(modifier = Modifier.height(8.dp))
            DayOfWeekHeader()
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val month = sortedMonths[page]
                val days = calendarData[month] ?: emptyList()
                MonthCalendar(month, days)
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
fun MonthCalendar(month: YearMonth, originalDays: List<CalendarDayData>) {
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        allCells.chunked(7).forEach { weekCells ->
            WeekRow(weekCells)
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
fun WeekRow(weekDays: List<CalendarDayData?>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(7) { index ->
            val dayData = if (index < weekDays.size) weekDays[index] else null
            Box(modifier = Modifier.weight(1f)) {
                if (dayData != null) {
                    DayCell(dayData)
                } else {
                    EmptyDayPlaceholder()
                }
            }
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
fun DayCell(dayData: CalendarDayData) {
    val isToday = dayData.date.isEqual(LocalDate.now())
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
            AlcoholUnitLevelProgressIndicator(
                modifier = Modifier.matchParentSize(),
                alcoholUnitLevel = it,
                strokeWidth = 3.dp,
            )
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
        CalendarScreenContent(previewData)
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
        CalendarScreenContent(previewData)
    }
}

private fun createPreviewCalendarData(): Map<YearMonth, List<CalendarDayData>> {
    val now = LocalDate.now()
    val result = mutableMapOf<YearMonth, List<CalendarDayData>>()

    for (i in 4 downTo 0) {
        val month = YearMonth.from(now).minusMonths(i.toLong())
        val days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            CalendarDayData(date, AlcoholUnitLevel.fromUnitCount(1f, limit = 3f))
        }
        result[month] = days
    }

    return result.toSortedMap(compareByDescending { it })
}
