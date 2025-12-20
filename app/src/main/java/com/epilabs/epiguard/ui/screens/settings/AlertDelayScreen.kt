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
import androidx.compose.runtime.mutableIntStateOf
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

import androidx.compose.ui.graphics.Brush

// Exact colors from the design
private val DarkBackground = Color(0xFF11222E)
private val CardBackground = Color(0xFF1A2A3A)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
// Vibrant gradient colors at full brightness
private val VibrantRed = Color(0xFFFF3B30)
private val VibrantOrange = Color(0xFFFF9500)
private val VibrantYellow = Color(0xFFFFCC00)
private val VibrantGreen = Color(0xFF34C759)
private val VibrantCyan = Color(0xFF5AC8FA)
private val VibrantBlue = Color(0xFF007AFF)
private val VibrantPurple = Color(0xFFAF52DE)

// Function to interpolate between colors for smooth gradient
private fun lerpColorAlert(start: Color, end: Color, fraction: Float): Color {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * clampedFraction,
        green = start.green + (end.green - start.green) * clampedFraction,
        blue = start.blue + (end.blue - start.blue) * clampedFraction
    )
}

private fun getColorForDelay(seconds: Int): Color {
    return when {
        seconds <= 6 -> {
            val factor = (seconds - 2).coerceIn(0, 4) / 4f
            lerpColorAlert(VibrantRed, VibrantOrange, factor)
        }
        seconds <= 12 -> {
            val factor = (seconds - 6) / 6f
            lerpColorAlert(VibrantOrange, VibrantYellow, factor)
        }
        seconds <= 20 -> {
            val factor = (seconds - 12) / 8f
            lerpColorAlert(VibrantYellow, VibrantGreen, factor)
        }
        seconds <= 30 -> {
            val factor = (seconds - 20) / 10f
            lerpColorAlert(VibrantGreen, VibrantCyan, factor)
        }
        seconds <= 50 -> {
            val factor = (seconds - 30) / 20f
            lerpColorAlert(VibrantCyan, VibrantBlue, factor)
        }
        else -> {
            val factor = ((seconds - 50).coerceAtMost(30)) / 30f
            lerpColorAlert(VibrantBlue, VibrantPurple, factor)
        }
    }
}

private fun getColorForInterval(interval: Int): Color {
    val normalized = (interval - 2) / 8f
    return when {
        normalized <= 0.2f -> lerpColorAlert(VibrantRed, VibrantOrange, normalized / 0.2f)
        normalized <= 0.4f -> lerpColorAlert(VibrantOrange, VibrantYellow, (normalized - 0.2f) / 0.2f)
        normalized <= 0.6f -> lerpColorAlert(VibrantYellow, VibrantGreen, (normalized - 0.4f) / 0.2f)
        normalized <= 0.8f -> lerpColorAlert(VibrantGreen, VibrantBlue, (normalized - 0.6f) / 0.2f)
        else -> lerpColorAlert(VibrantBlue, VibrantPurple, (normalized - 0.8f) / 0.2f)
    }
}

private fun getColorForConsecutive(limit: Int): Color {
    val normalized = (limit - 1) / 7f
    return when {
        normalized <= 0.2f -> lerpColorAlert(VibrantRed, VibrantOrange, normalized / 0.2f)
        normalized <= 0.4f -> lerpColorAlert(VibrantOrange, VibrantYellow, (normalized - 0.2f) / 0.2f)
        normalized <= 0.6f -> lerpColorAlert(VibrantYellow, VibrantGreen, (normalized - 0.4f) / 0.2f)
        normalized <= 0.8f -> lerpColorAlert(VibrantGreen, VibrantBlue, (normalized - 0.6f) / 0.2f)
        else -> lerpColorAlert(VibrantBlue, VibrantPurple, (normalized - 0.8f) / 0.2f)
    }
}

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
                        "Alert Delay",
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
                text = "Alert Timing Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Configure how long the system waits before sending seizure alerts. This helps prevent false alarms from brief anomalies.",
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Total Delay Display with vibrant gradient
            val delayColor = getColorForDelay(totalDelaySeconds)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = delayColor.copy()
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
                            text = "Total Alert Delay",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${totalDelaySeconds}s",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "$consecutiveLimit consecutive detections × ${inferenceInterval}s intervals",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Detection Interval Slider
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
                        text = "Detection Interval",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "How often the AI analyzes video frames for seizure activity",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current: ${inferenceInterval}s",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    val intervalColor = getColorForInterval(inferenceInterval)
                    Slider(
                        value = inferenceInterval.toFloat(),
                        onValueChange = { inferenceInterval = it.toInt() },
                        valueRange = 2f..10f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = intervalColor,
                            activeTrackColor = intervalColor,
                            inactiveTrackColor = TextFieldBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "2s\nFast",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "10s\nSlow",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Consecutive Detections Slider
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
                        text = "Consecutive Detections Required",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Number of consecutive positive detections needed before sending an alert",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current: $consecutiveLimit detections",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    val consecutiveColor = getColorForConsecutive(consecutiveLimit)
                    Slider(
                        value = consecutiveLimit.toFloat(),
                        onValueChange = { consecutiveLimit = it.toInt() },
                        valueRange = 1f..8f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = consecutiveColor,
                            activeTrackColor = consecutiveColor,
                            inactiveTrackColor = TextFieldBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1\nImmediate",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "8\nVery Delayed",
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
                        text = "• The system analyzes video every ${inferenceInterval} seconds\n" +
                                "• It needs $consecutiveLimit positive detections in a row\n" +
                                "• Total delay before alert: ${totalDelaySeconds} seconds\n" +
                                "• This prevents false alarms from brief movements or shadows\n" +
                                "• Adjust based on your seizure patterns and environment",
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
                        text = "Recommended settings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = when {
                            totalDelaySeconds <= 10 -> "⚡ Quick Response (${totalDelaySeconds}s) - Good for severe seizures, may have false alarms"
                            totalDelaySeconds <= 20 -> "⚖️ Balanced (${totalDelaySeconds}s) - Recommended for most users"
                            else -> "🛡️ Conservative (${totalDelaySeconds}s) - Fewer false alarms, slower response"
                        },
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}