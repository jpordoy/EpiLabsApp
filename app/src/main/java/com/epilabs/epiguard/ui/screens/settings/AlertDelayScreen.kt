package com.epilabs.epiguard.ui.screens.settings

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.PreferencesHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDelayScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesHelper = remember { PreferencesHelper(context) }

    var consecutiveLimit by remember {
        mutableIntStateOf(preferencesHelper.getConsecutiveLimit())
    }
    var inferenceInterval by remember {
        mutableIntStateOf((preferencesHelper.getInferenceInterval() / 1000).toInt())
    }

    // Save changes when values change
    LaunchedEffect(consecutiveLimit, inferenceInterval) {
        preferencesHelper.setConsecutiveLimit(consecutiveLimit)
        preferencesHelper.setInferenceInterval((inferenceInterval * 1000).toLong())
    }

    val totalDelaySeconds = consecutiveLimit * inferenceInterval

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert Delay") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
            // Header
            Text(
                text = "Alert Timing Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Configure how long the system waits before sending seizure alerts. This helps prevent false alarms from brief anomalies.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Total Delay Display
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        totalDelaySeconds >= 30 -> MaterialTheme.colorScheme.tertiaryContainer
                        totalDelaySeconds >= 15 -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Alert Delay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${totalDelaySeconds}s",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$consecutiveLimit consecutive detections × ${inferenceInterval}s intervals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Detection Interval Slider
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detection Interval",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "How often the AI analyzes video frames for seizure activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current: ${inferenceInterval}s",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Slider(
                        value = inferenceInterval.toFloat(),
                        onValueChange = { inferenceInterval = it.toInt() },
                        valueRange = 2f..10f,
                        steps = 7, // 2, 3, 4, 5, 6, 7, 8, 9, 10
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "2s\nFast",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "10s\nSlow",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Consecutive Detections Slider
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Consecutive Detections Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Number of consecutive positive detections needed before sending an alert",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current: $consecutiveLimit detections",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Slider(
                        value = consecutiveLimit.toFloat(),
                        onValueChange = { consecutiveLimit = it.toInt() },
                        valueRange = 1f..8f,
                        steps = 6, // 1, 2, 3, 4, 5, 6, 7, 8
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1\nImmediate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "8\nVery Delayed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Information Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "• The system analyzes video every ${inferenceInterval} seconds\n" +
                                "• It needs $consecutiveLimit positive detections in a row\n" +
                                "• Total delay before alert: ${totalDelaySeconds} seconds\n" +
                                "• This prevents false alarms from brief movements or shadows\n" +
                                "• Adjust based on your seizure patterns and environment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Recommendations Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recommended settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = when {
                            totalDelaySeconds <= 10 -> "⚡ Quick Response (${totalDelaySeconds}s) - Good for severe seizures, may have false alarms"
                            totalDelaySeconds <= 20 -> "⚖️ Balanced (${totalDelaySeconds}s) - Recommended for most users"
                            else -> "🛡️ Conservative (${totalDelaySeconds}s) - Fewer false alarms, slower response"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}