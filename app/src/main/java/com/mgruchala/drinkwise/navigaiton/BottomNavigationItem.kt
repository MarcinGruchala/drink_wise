package com.mgruchala.drinkwise.navigaiton

import androidx.annotation.DrawableRes
import com.mgruchala.drinkwise.R

data class BottomNavigationItem(
    val name: String,
    val route: AppRoute,
    @DrawableRes val activeIconAssetId: Int,
    @DrawableRes val inactiveIconAssetId: Int
)

val bottomNavigationItems = listOf(
    BottomNavigationItem(
        name = "Home",
        route = AppRoute.Home,
        activeIconAssetId = R.drawable.ic_home_filled,
        inactiveIconAssetId = R.drawable.ic_home
    ),
    BottomNavigationItem(
        name = "Calendar",
        route = AppRoute.Calendar,
        activeIconAssetId = R.drawable.ic_calendar_filled,
        inactiveIconAssetId = R.drawable.ic_calendar
    ),
    BottomNavigationItem(
        name = "Calculator",
        route = AppRoute.Calculator,
        activeIconAssetId = R.drawable.ic_calculate_filled,
        inactiveIconAssetId = R.drawable.ic_calculate
    ),
    BottomNavigationItem(
        name = "Settings",
        route = AppRoute.Settings,
        activeIconAssetId = R.drawable.ic_settings_filled,
        inactiveIconAssetId = R.drawable.ic_settings
    )
)
