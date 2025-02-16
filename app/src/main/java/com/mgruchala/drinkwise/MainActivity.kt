package com.mgruchala.drinkwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mgruchala.drinkwise.navigaiton.AppNavigation
import com.mgruchala.drinkwise.theme.DrinkWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrinkWiseTheme {
                AppNavigation()
            }
        }
    }
}

