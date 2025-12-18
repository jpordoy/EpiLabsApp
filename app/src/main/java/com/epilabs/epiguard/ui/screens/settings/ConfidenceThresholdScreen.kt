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
import androidx.compose.runtime.mutableFloatStateOf
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
fun ConfidenceThresholdScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesHelper = remember { PreferencesHelper(context) }

    var confidenceThreshold by remember {
        mutableFloatStateOf(preferencesHelper.getConfidenceThreshold())
    }

    // Save changes when threshold changes
    LaunchedEffect(confidenceThreshold) {
        preferencesHelper.setConfidenceThreshold(confidenceThreshold)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confidence Threshold") },
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
                text = "AI Model Confidence",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Adjust how confident the AI model must be before detecting a seizure. Higher values reduce false positives but may miss some seizures.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Value Display
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        confidenceThreshold >= 0.9f -> MaterialTheme.colorScheme.errorContainer
                        confidenceThreshold >= 0.8f -> MaterialTheme.colorScheme.tertiaryContainer
                        confidenceThreshold >= 0.7f -> MaterialTheme.colorScheme.primaryContainer
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
                            text = "Current Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(confidenceThreshold * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = when {
                            confidenceThreshold >= 0.9f -> "Very High - Minimal false positives, may miss subtle seizures"
                            confidenceThreshold >= 0.8f -> "High - Good balance, recommended for most users"
                            confidenceThreshold >= 0.7f -> "Medium - More sensitive, may have occasional false positives"
                            else -> "Low - Very sensitive, likely to have false positives"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Slider
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Threshold Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Slider(
                        value = confidenceThreshold,
                        onValueChange = { confidenceThreshold = it },
                        valueRange = 0.5f..0.95f,
                        steps = 8, // 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "50%\nMore Sensitive",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "95%\nLess Sensitive",
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
                        text = "• The AI model gives each prediction a confidence score from 0-100%\n" +
                                "• Only predictions above your threshold trigger seizure alerts\n" +
                                "• Higher thresholds = fewer false alarms but may miss seizures\n" +
                                "• Lower thresholds = catch more seizures but more false alarms\n" +
                                "• Recommended: Start at 80% and adjust based on your experience",
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
                        text = "Recommendations by seizure type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "• Tonic-Clonic (Grand Mal): 75-85% - Clear movements, easier to detect\n" +
                                "• Focal Seizures: 80-90% - Subtle movements, reduce false positives\n" +
                                "• Absence Seizures: 65-75% - Minimal movement, need high sensitivity\n" +
                                "• Myoclonic: 70-80% - Quick jerks, balance sensitivity and accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}