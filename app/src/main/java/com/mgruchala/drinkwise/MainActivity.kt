package com.mgruchala.drinkwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mgruchala.alcohol_database.utils.DatabaseInitializer
import com.mgruchala.drinkwise.navigaiton.AppNavigation
import com.mgruchala.drinkwise.presentation.theme.DrinkWiseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        databaseInitializer.initializeIfNeeded()
        setContent {
            DrinkWiseTheme {
                AppNavigation()
            }
        }
    }
}

