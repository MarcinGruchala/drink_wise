package com.mgruchala.drinkwise.navigaiton

import androidx.annotation.DrawableRes
import com.mgruchala.drinkwise.R
import androidx.annotation.StringRes

data class BottomNavigationItem(
    @StringRes val nameRes: Int,
    val route: AppRoute,
    @DrawableRes val activeIconAssetId: Int,
    @DrawableRes val inactiveIconAssetId: Int
)

val bottomNavigationItems = listOf(
    BottomNavigationItem(
        nameRes = R.string.bottom_navigation_home,
        route = AppRoute.Home,
        activeIconAssetId = R.drawable.ic_home_filled,
        inactiveIconAssetId = R.drawable.ic_home
    ),
    BottomNavigationItem(
        nameRes = R.string.bottom_navigation_calendar,
        route = AppRoute.Calendar,
        activeIconAssetId = R.drawable.ic_calendar_filled,
        inactiveIconAssetId = R.drawable.ic_calendar
    ),
    BottomNavigationItem(
        nameRes = R.string.bottom_navigation_calculator,
        route = AppRoute.Calculator,
        activeIconAssetId = R.drawable.ic_calculate_filled,
        inactiveIconAssetId = R.drawable.ic_calculate
    ),
    BottomNavigationItem(
        nameRes = R.string.bottom_navigation_settings,
        route = AppRoute.Settings,
        activeIconAssetId = R.drawable.ic_settings_filled,
        inactiveIconAssetId = R.drawable.ic_settings
    )
)
