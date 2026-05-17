package com.mgruchala.drinkwise.navigaiton

sealed class AppRoute(val name: String) {
    data object Home : AppRoute("home")
    data object Calendar : AppRoute("calendar")
    data object Calculator : AppRoute("calculator")
    data object Settings : AppRoute("settings")
    data object DayDetails : AppRoute("day_details/{epochDay}") {
        const val ARG_EPOCH_DAY = "epochDay"
        fun createRoute(epochDay: Long): String = "day_details/$epochDay"
    }
}
