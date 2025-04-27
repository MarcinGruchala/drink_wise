package com.mgruchala.drinkwise.presentation.calendar

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

// Mock Data Structure
data class CalendarDayData(
    val date: LocalDate,
    val hasDrinks: Boolean,
    val drinkCount: Int = 0
)

// Mock Data Generation (Example for 3 months)
fun generateMockCalendarData(startMonth: YearMonth, months: Int): List<CalendarDayData> {
    val data = mutableListOf<CalendarDayData>()
    var currentMonth = startMonth
    repeat(months) {
        val daysInMonth = currentMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = currentMonth.atDay(day)
            // Ensure we have some true values for preview
            val hasDrinks = Random.nextDouble() < 0.3 // ~30% chance of having drinks
            val drinkCount = if (hasDrinks) Random.nextInt(1, 5) else 0
            data.add(CalendarDayData(date, hasDrinks, drinkCount))
        }
        currentMonth = currentMonth.plusMonths(1)
    }
    return data
}

val mockCalendarData = generateMockCalendarData(YearMonth.now().minusMonths(2), 3)

@Composable
fun CalendarScreen(
    // viewModel: CalendarViewModel = hiltViewModel() // ViewModel integration later
) {
    // val state by viewModel.state.collectAsState() // ViewModel integration later

    // Use mock data for now
    val calendarData = mockCalendarData.groupBy { YearMonth.from(it.date) }
        .toSortedMap(compareByDescending { it }) // Sort months descending: newest first

    CalendarScreenContent(calendarData = calendarData)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenContent(calendarData: Map<YearMonth, List<CalendarDayData>>) {
    Scaffold {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                DayOfWeekHeader()
                Spacer(modifier = Modifier.height(8.dp))
            }

            calendarData.forEach { (month, days) ->
                item {
                    MonthHeader(month)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Group days by week, handling padding for the first week
                val firstDayOfMonth = days.first().date
                val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7) // Monday=1..Sunday=7 -> 0..6 ? Let's assume week starts Monday (1) -> Sunday (7)
                                                                              // We want offset from Monday. Monday=1 -> offset 0, Sunday=7 -> offset 6
                val startPaddingCells = firstDayOfMonth.dayOfWeek.value - 1 // Monday = 0 padding, Sunday = 6 padding

                val allCells = mutableListOf<CalendarDayData?>()
                repeat(startPaddingCells) { allCells.add(null) } // Add null placeholders for padding
                allCells.addAll(days)

                items(allCells.chunked(7)) { weekCells ->
                    WeekRow(weekCells)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp)) // Increased space between months
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
        // Assuming Monday is the start of the week
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp), // Match DayCell width
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}


@Composable
fun MonthHeader(month: YearMonth) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    Text(
        text = month.format(formatter),
        style = MaterialTheme.typography.titleLarge, // Slightly larger title
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
                Spacer(modifier = Modifier.size(40.dp)) // Placeholder for empty days
            }
        }
        // Add spacers if the week is not full (end of the month)
        val remainingCells = 7 - weekDays.size
        repeat(remainingCells) {
            Spacer(modifier = Modifier.size(40.dp)) // Placeholder size
        }
    }
}

@Composable
fun DayCell(dayData: CalendarDayData) {
    Box(
        modifier = Modifier
            .size(40.dp) // Consistent size
            .background(
                color = when {
                    // Optional: Highlight today's date
                    // dayData.date.isEqual(LocalDate.now()) -> MaterialTheme.colorScheme.tertiaryContainer
                    dayData.hasDrinks -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // More subtle background for empty days
                },
                shape = MaterialTheme.shapes.medium // Slightly rounded corners
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayData.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (dayData.hasDrinks) FontWeight.Bold else FontWeight.Normal,
            color = when {
                // Optional: Highlight today's date text
                // dayData.date.isEqual(LocalDate.now()) -> MaterialTheme.colorScheme.onTertiaryContainer
                dayData.hasDrinks -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        // Optional: Add drink count indicator if needed later
        // if (dayData.hasDrinks && dayData.drinkCount > 0) {
        //     Box(modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)) {
        //         Text(dayData.drinkCount.toString(), fontSize = 8.sp, color = MaterialTheme.colorScheme.secondary)
        //     }
        // }
    }
}


// --- Previews ---

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7_PRO,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun CalendarScreenPreviewLightTheme() {
    DrinkWiseTheme {
        CalendarScreen() // Use the main entry point for preview
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
        CalendarScreen() // Use the main entry point for preview
    }
}

// Keep dummy data for potential future use or reference, but it's not used by the calendar view previews currently
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
