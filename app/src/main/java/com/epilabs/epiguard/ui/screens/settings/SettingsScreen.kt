package com.epilabs.epiguard.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.OnboardingHelper
import com.epilabs.epiguard.utils.PreferencesHelper

// Exact colors from the design
private val DarkBackground = Color(0xFF11222E)
private val CardBackground = Color(0xFF1A2A3A)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)

@Composable
fun SettingsScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesHelper = remember { PreferencesHelper(context) }
    val settings = remember { preferencesHelper.getAllSettings() }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }

    // Set system bars to dark
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    LaunchedEffect(Unit) {
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.statusBarColor = DarkBackground.toArgb()
            it.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
        }
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Notification Settings
            SettingsSection(
                title = "Notifications",
                icon = Icons.Default.Notifications
            ) {
                SettingsToggle(
                    title = "Push Notifications",
                    description = "Receive alerts when seizures are detected",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                SettingsToggle(
                    title = "Sound",
                    description = "Play sound for notifications",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )

                SettingsToggle(
                    title = "Vibration",
                    description = "Vibrate for notifications",
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }

            // Detection Settings
            SettingsSection(
                title = "Detection",
                icon = Icons.Default.Tune
            ) {
                SettingsItem(
                    title = "Confidence Threshold",
                    description = "Currently ${(settings.confidenceThreshold * 100).toInt()}% - AI confidence required for detection",
                    onClick = { navController.navigate("confidence_threshold") }
                )

                SettingsItem(
                    title = "Alert Delay",
                    description = "Currently ${settings.totalDelaySeconds}s - Time before sending alerts",
                    onClick = { navController.navigate("alert_delay") }
                )

                SettingsItem(
                    title = "Model Settings",
                    description = "Configure AI model parameters",
                    onClick = { /* Navigate to model settings */ }
                )
            }

            // Privacy & Security
            SettingsSection(
                title = "Privacy & Security",
                icon = Icons.Default.Security
            ) {
                SettingsItem(
                    title = "Data Privacy",
                    description = "Manage your data and privacy settings",
                    onClick = { /* Navigate to data privacy */ }
                )

                SettingsItem(
                    title = "Export Data",
                    description = "Download your detection history",
                    onClick = { /* Export data */ }
                )

                SettingsItem(
                    title = "Delete Account",
                    description = "Permanently delete your account",
                    onClick = { /* Navigate to account deletion */ }
                )
            }

            // About
            SettingsSection(
                title = "About",
                icon = Icons.Default.Info
            ) {
                SettingsItem(
                    title = "App Version",
                    description = "1.0.0 (Build 1)",
                    onClick = { }
                )

                SettingsItem(
                    title = "Privacy Policy",
                    description = "Read our privacy policy",
                    onClick = { navController.navigate("privacy_policy") }
                )

                SettingsItem(
                    title = "Terms of Service",
                    description = "Read our terms of service",
                    onClick = { navController.navigate("terms_of_service") }
                )

                SettingsItem(
                    title = "Help & Support",
                    description = "Get help with the app",
                    onClick = { navController.navigate("help_support") }
                )
            }

            // Development
            SettingsSection(
                title = "Development",
                icon = Icons.Default.Build
            ) {
                SettingsItem(
                    title = "Reset Onboarding",
                    description = "Show onboarding flow again on next app launch",
                    onClick = {
                        OnboardingHelper.resetOnboarding(context)
                    }
                )

                SettingsItem(
                    title = "Show Onboarding Now",
                    description = "Navigate to onboarding screen immediately",
                    onClick = {
                        navController.navigate(Destinations.Onboarding.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ButtonBlue
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ButtonBlue
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = TextSecondary
            )
        }
    }
    HorizontalDivider(color = TextFieldBorder)
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = ButtonBlue,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TextFieldBorder
                )
            )
        }
    }
    HorizontalDivider(color = TextFieldBorder)
}