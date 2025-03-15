package com.mgruchala.drinkwise.navigaiton

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mgruchala.drinkwise.presentation.calculator.AlcoholCalculatorView
import com.mgruchala.drinkwise.presentation.calendar.CalendarScreen
import com.mgruchala.drinkwise.presentation.home.HomeScreen
import com.mgruchala.drinkwise.presentation.settings.SettingsScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

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
                            androidx.compose.material3.Icon(
                                painter = painterResource(iconAssetId),
                                contentDescription = bottomNavigationItem.name
                            )
                        },
                        label = {
                            androidx.compose.material3.Text(bottomNavigationItem.name)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Home.name) { HomeScreen() }
            composable(AppRoute.Calendar.name) { CalendarScreen() }
            composable(AppRoute.Calculator.name) { AlcoholCalculatorView() }
            composable(AppRoute.Settings.name) { SettingsScreen() }
        }
    }
}
