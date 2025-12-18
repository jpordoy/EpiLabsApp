package com.epilabs.epiguard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.epilabs.epiguard.ui.nav.Destinations
import android.util.Log

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Destinations.Dashboard.route,
            onClick = {
                if (currentRoute != Destinations.Dashboard.route) {
                    try {
                        navController.navigate(Destinations.Dashboard.route) {
                            popUpTo(Destinations.Dashboard.route) { inclusive = true }
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationError", "Failed to navigate to dashboard: ${e.message}")
                    }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Destinations.DeviceSelection.route,
            onClick = {
                if (currentRoute != Destinations.DeviceSelection.route) {
                    try {
                        navController.navigate(Destinations.DeviceSelection.route)
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationError", "Failed to navigate to device_selection: ${e.message}")
                    }
                }
            },
            icon = { Icon(Icons.Default.Visibility, contentDescription = "Detection") },
            label = { Text("Detection") }
        )

        NavigationBarItem(
            selected = currentRoute == Destinations.Contacts.route,
            onClick = {
                if (currentRoute != Destinations.Contacts.route) {
                    try {
                        navController.navigate(Destinations.Contacts.route)
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationError", "Failed to navigate to contacts: ${e.message}")
                    }
                }
            },
            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
            label = { Text("Contacts") }
        )

        NavigationBarItem(
            selected = currentRoute == Destinations.Settings.route,
            onClick = {
                if (currentRoute != Destinations.Settings.route) {
                    try {
                        navController.navigate(Destinations.Settings.route)
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationError", "Failed to navigate to settings: ${e.message}")
                    }
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}