package com.mgruchala.drinkwise.navigaiton

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mgruchala.drinkwise.presentation.calculator.AlcoholCalculatorView
import com.mgruchala.drinkwise.presentation.calendar.CalendarScreen
import com.mgruchala.drinkwise.presentation.daydetails.DayDetailsScreen
import com.mgruchala.drinkwise.presentation.home.HomeScreen
import com.mgruchala.drinkwise.presentation.settings.SettingsScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var homeInitialProgressAnimationShown by rememberSaveable { mutableStateOf(false) }
    var calendarInitialProgressAnimationShown by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val shouldShowBottomBar = currentRoute != AppRoute.DayDetails.name

            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavigationItems.forEach { bottomNavigationItem ->
                        val selected = currentRoute == bottomNavigationItem.route.name
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(bottomNavigationItem.route.name) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                val iconAssetId = if (selected) {
                                    bottomNavigationItem.activeIconAssetId
                                } else {
                                    bottomNavigationItem.inactiveIconAssetId
                                }
                                Icon(
                                    painter = painterResource(iconAssetId),
                                    contentDescription = stringResource(bottomNavigationItem.nameRes)
                                )
                            },
                            label = {
                                Text(stringResource(bottomNavigationItem.nameRes))
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.name,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(AppRoute.Home.name) {
                HomeScreen(
                    animateInitialProgress = !homeInitialProgressAnimationShown,
                    onInitialProgressAnimationConsumed = {
                        homeInitialProgressAnimationShown = true
                    }
                )
            }
            composable(AppRoute.Calendar.name) {
                CalendarScreen(
                    animateInitialProgress = !calendarInitialProgressAnimationShown,
                    onInitialProgressAnimationConsumed = {
                        calendarInitialProgressAnimationShown = true
                    },
                    onDayClick = { date ->
                        navController.navigate(AppRoute.DayDetails.createRoute(date.toEpochDay()))
                    }
                )
            }
            composable(AppRoute.Calculator.name) { AlcoholCalculatorView() }
            composable(AppRoute.Settings.name) { SettingsScreen() }
            composable(
                route = AppRoute.DayDetails.name,
                arguments = listOf(
                    navArgument(AppRoute.DayDetails.ARG_EPOCH_DAY) { type = NavType.LongType }
                )
            ) {
                DayDetailsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
