package com.mgruchala.drinkwise.presentation.calendar

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mgruchala.drinkwise.domain.AlcoholUnitLevel
import com.mgruchala.drinkwise.presentation.common.AlcoholUnitLevelProgressIndicator
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
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
    Scaffold {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            calendarData.forEach { (month, originalDays) ->
                item {
                    MonthHeader(month)
                    Spacer(modifier = Modifier.height(8.dp))
                    DayOfWeekHeader()
                    Spacer(modifier = Modifier.height(8.dp))
                }

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

                items(allCells.chunked(7)) { weekCells ->
                    WeekRow(weekCells)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DayOfWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MonthHeader(month: YearMonth) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    Text(
        text = month.format(formatter),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun WeekRow(weekDays: List<CalendarDayData?>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        weekDays.forEach { dayData ->
            if (dayData != null) {
                DayCell(dayData)
            } else {
                EmptyDayPlaceholder()
            }
        }
        val remainingCells = 7 - weekDays.size
        repeat(remainingCells) {
            EmptyDayPlaceholder()
        }
    }
}

@Composable
fun EmptyDayPlaceholder() {
    Spacer(modifier = Modifier.size(40.dp))
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
            .size(40.dp)
            .then(backgroundModifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayData.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )

        dayData.alcoholUnitLevel?.let {
            AlcoholUnitLevelProgressIndicator(
                modifier = Modifier.matchParentSize(),
                alcoholUnitLevel = it,
                strokeWidth = 2.5.dp,
            )
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
        val previewData = createPreviewCalendarData()
        CalendarScreenContent(previewData)
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
        // Use preview data for the preview
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
