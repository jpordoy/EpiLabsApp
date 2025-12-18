package com.epilabs.epiguard.ui.screens.settings

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.OnboardingHelper
import com.epilabs.epiguard.utils.PreferencesHelper

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

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card {
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
    HorizontalDivider()
}