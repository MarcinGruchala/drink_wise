package com.mgruchala.drinkwise.presentation.daydetails.components

import android.content.Context
import com.mgruchala.drinkwise.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Formats a date for display in the Day Details screen.
 */
object DateFormatter {

    private val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    private val collapsedDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    /**
     * Full date format for expanded app bar: "Monday, January 27, 2026"
     */
    fun formatFullDate(date: LocalDate): String {
        return date.format(fullDateFormatter)
    }

    /**
     * Collapsed date format for collapsed app bar: "Mon, Jan 27"
     */
    fun formatCollapsedDate(date: LocalDate): String {
        return date.format(collapsedDateFormatter)
    }

    /**
     * Relative time description: "Today", "Yesterday", "6 months ago", etc.
     */
    fun formatRelativeTime(date: LocalDate, context: Context): String {
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(date, today)

        return when {
            daysDiff == 0L -> context.getString(R.string.relative_time_today)
            daysDiff == 1L -> context.getString(R.string.relative_time_yesterday)
            daysDiff < 7L -> context.getString(R.string.relative_time_days_ago, daysDiff.toInt())
            daysDiff < 30L -> {
                val weeks = (daysDiff / 7).toInt()
                context.getString(R.string.relative_time_weeks_ago, weeks)
            }
            daysDiff < 365L -> {
                val months = (daysDiff / 30).toInt()
                context.getString(R.string.relative_time_months_ago, months)
            }
            else -> {
                val years = (daysDiff / 365).toInt()
                context.getString(R.string.relative_time_years_ago, years)
            }
        }
    }
}
