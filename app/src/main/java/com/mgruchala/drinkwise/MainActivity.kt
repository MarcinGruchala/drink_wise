package com.mgruchala.drinkwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mgruchala.drinkwise.calculator.AlcoholCalculatorView
import com.mgruchala.drinkwise.calendar.CalendarScreen
import com.mgruchala.drinkwise.home.HomeScreen
import com.mgruchala.drinkwise.settings.SettingsScreen
import com.mgruchala.drinkwise.ui.theme.DrinkWiseTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrinkWiseTheme {
                val navController = rememberNavController()
                var bottomNavSelectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            bottomNavigationItems.forEachIndexed { index, bottomNavigationItem ->
                                NavigationBarItem(
                                    selected = index == bottomNavSelectedItemIndex,
                                    onClick = {
                                        bottomNavSelectedItemIndex = index
                                        navController.navigate(bottomNavigationItem.route)
                                    },
                                    icon = {
                                        val iconAssetId = if (index == bottomNavSelectedItemIndex) {
                                            bottomNavigationItem.activeIconAssetId
                                        } else {
                                            bottomNavigationItem.inactiveIconAssetId
                                        }
                                        Icon(
                                            painter = painterResource(iconAssetId),
                                            contentDescription = bottomNavigationItem.name
                                        )
                                    },
                                    label = {
                                        Text(bottomNavigationItem.name)
                                    },
                                )

                            }
                        }
                    },
                    content = { innerPadding ->
                        NavHost(
                            navController,
                            startDestination = HomeRoute,
                            Modifier.padding(innerPadding)
                        ) {
                            composable<HomeRoute> {
                                HomeScreen()
                            }
                            composable<CalendarRoute> {
                                CalendarScreen()
                            }
                            composable<CalculatorRoute> {
                                AlcoholCalculatorView()
                            }
                            composable<SettingsRoute> {
                                SettingsScreen()
                            }

                        }
                    }
                )
            }
        }
    }
}

@Serializable
object HomeRoute

@Serializable
object CalendarRoute

@Serializable
object CalculatorRoute

@Serializable
object SettingsRoute

data class BottomNavigationItem<T : Any>(
    val name: String,
    val route: T,
    val activeIconAssetId: Int,
    val inactiveIconAssetId: Int
)

val bottomNavigationItems = listOf(
    BottomNavigationItem(
        name = "Home",
        route = HomeRoute,
        activeIconAssetId = R.drawable.ic_home_filled,
        inactiveIconAssetId = R.drawable.ic_home
    ),
    BottomNavigationItem(
        name = "Calendar",
        route = CalendarRoute,
        activeIconAssetId = R.drawable.ic_calendar_filled,
        inactiveIconAssetId = R.drawable.ic_calendar
    ),
    BottomNavigationItem(
        name = "Calculator",
        route = CalculatorRoute,
        activeIconAssetId = R.drawable.ic_calculate_filled,
        inactiveIconAssetId = R.drawable.ic_calculate
    ),
    BottomNavigationItem(
        name = "Settings",
        route = SettingsRoute,
        activeIconAssetId = R.drawable.ic_settings_filled,
        inactiveIconAssetId = R.drawable.ic_settings
    )
)
