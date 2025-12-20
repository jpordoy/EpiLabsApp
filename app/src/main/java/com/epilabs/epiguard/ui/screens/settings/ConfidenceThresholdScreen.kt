package com.epilabs.epiguard.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.PreferencesHelper

// Exact colors from the design
private val DarkBackground = Color(0xFF11222E)
private val CardBackground = Color(0xFF1A2A3A)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
// Vibrant gradient colors
private val VibrantRed = Color(0xFFFF3B30)
private val VibrantOrange = Color(0xFFFF9500)
private val VibrantYellow = Color(0xFFFFCC00)
private val VibrantGreen = Color(0xFF34C759)
private val VibrantBlue = Color(0xFF007AFF)
private val VibrantPurple = Color(0xFFAF52DE)

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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Confidence Threshold",
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
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
            // Header
            Text(
                text = "AI Model Confidence",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Adjust how confident the AI model must be before detecting a seizure. Higher values reduce false positives but may miss some seizures.",
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Value Display with vibrant gradient
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        confidenceThreshold >= 0.9f -> VibrantPurple.copy()
                        confidenceThreshold >= 0.85f -> VibrantBlue.copy()
                        confidenceThreshold >= 0.8f -> VibrantGreen.copy()
                        confidenceThreshold >= 0.7f -> VibrantYellow.copy()
                        confidenceThreshold >= 0.6f -> VibrantOrange.copy()
                        else -> VibrantRed.copy()
                    }
                ),
                shape = RoundedCornerShape(10.dp)
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(confidenceThreshold * 100).toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = when {
                            confidenceThreshold >= 0.9f -> "Very High - Minimal false positives, may miss subtle seizures"
                            confidenceThreshold >= 0.8f -> "High - Good balance, recommended for most users"
                            confidenceThreshold >= 0.7f -> "Medium - More sensitive, may have occasional false positives"
                            else -> "Low - Very sensitive, likely to have false positives"
                        },
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Slider
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Threshold Level",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Slider(
                        value = confidenceThreshold,
                        onValueChange = { confidenceThreshold = it },
                        valueRange = 0.5f..0.95f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = when {
                                confidenceThreshold >= 0.9f -> VibrantPurple
                                confidenceThreshold >= 0.85f -> VibrantBlue
                                confidenceThreshold >= 0.8f -> VibrantGreen
                                confidenceThreshold >= 0.7f -> VibrantYellow
                                confidenceThreshold >= 0.6f -> VibrantOrange
                                else -> VibrantRed
                            },
                            activeTrackColor = when {
                                confidenceThreshold >= 0.9f -> VibrantPurple
                                confidenceThreshold >= 0.85f -> VibrantBlue
                                confidenceThreshold >= 0.8f -> VibrantGreen
                                confidenceThreshold >= 0.7f -> VibrantYellow
                                confidenceThreshold >= 0.6f -> VibrantOrange
                                else -> VibrantRed
                            },
                            inactiveTrackColor = TextFieldBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "50%\nMore Sensitive",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "95%\nLess Sensitive",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Information Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How it works",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "• The AI model gives each prediction a confidence score from 0-100%\n" +
                                "• Only predictions above your threshold trigger seizure alerts\n" +
                                "• Higher thresholds = fewer false alarms but may miss seizures\n" +
                                "• Lower thresholds = catch more seizures but more false alarms\n" +
                                "• Recommended: Start at 80% and adjust based on your experience",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Recommendations Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ButtonBlue.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recommendations by seizure type",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "• Tonic-Clonic (Grand Mal): 75-85% - Clear movements, easier to detect\n" +
                                "• Focal Seizures: 80-90% - Subtle movements, reduce false positives\n" +
                                "• Absence Seizures: 65-75% - Minimal movement, need high sensitivity\n" +
                                "• Myoclonic: 70-80% - Quick jerks, balance sensitivity and accuracy",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}