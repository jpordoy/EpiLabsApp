package com.epilabs.epiguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.nav.NavGraph
import com.epilabs.epiguard.ui.theme.EpiGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EpiGuardTheme {
                val navController = rememberNavController()


                NavGraph(
                    navController = navController,
                    startDestination = Destinations.Login.route
                )
            }
        }
    }
}