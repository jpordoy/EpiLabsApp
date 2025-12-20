package com.epilabs.epiguard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.epilabs.epiguard.ui.nav.Destinations
import android.util.Log
import androidx.compose.ui.graphics.Color

// Hardcoded colors from design
private val DarkBackground = Color(0xFF11222E)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
private val ButtonTextBlue = Color(0xFF0C5AC7)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentOrange = Color(0xFFFF9800)
private val AccentPurple = Color(0xFF9C27B0)
private val UnreadIndicator = Color(0xFF0163E1)

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = DarkBackground
    ) {
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
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentRoute == Destinations.Dashboard.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (currentRoute == Destinations.Dashboard.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ButtonTextBlue,
                selectedTextColor = ButtonTextBlue,
                unselectedIconColor = TextFieldPlaceholder,
                unselectedTextColor = TextFieldPlaceholder,
                indicatorColor = Color.Transparent
            )
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
            icon = {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Detection",
                    tint = if (currentRoute == Destinations.DeviceSelection.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            label = {
                Text(
                    "Detection",
                    color = if (currentRoute == Destinations.DeviceSelection.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ButtonTextBlue,
                selectedTextColor = ButtonTextBlue,
                unselectedIconColor = TextFieldPlaceholder,
                unselectedTextColor = TextFieldPlaceholder,
                indicatorColor = Color.Transparent
            )
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
            icon = {
                Icon(
                    Icons.Default.Contacts,
                    contentDescription = "Contacts",
                    tint = if (currentRoute == Destinations.Contacts.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            label = {
                Text(
                    "Contacts",
                    color = if (currentRoute == Destinations.Contacts.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ButtonTextBlue,
                selectedTextColor = ButtonTextBlue,
                unselectedIconColor = TextFieldPlaceholder,
                unselectedTextColor = TextFieldPlaceholder,
                indicatorColor = Color.Transparent
            )
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
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (currentRoute == Destinations.Settings.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            label = {
                Text(
                    "Settings",
                    color = if (currentRoute == Destinations.Settings.route) ButtonTextBlue else TextFieldPlaceholder
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ButtonTextBlue,
                selectedTextColor = ButtonTextBlue,
                unselectedIconColor = TextFieldPlaceholder,
                unselectedTextColor = TextFieldPlaceholder,
                indicatorColor = Color.Transparent
            )
        )
    }
}