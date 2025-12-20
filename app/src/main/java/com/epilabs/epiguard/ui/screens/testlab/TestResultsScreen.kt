// Updated TestResultsScreen.kt
package com.epilabs.epiguard.ui.screens.testlab

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.R
import com.epilabs.epiguard.models.VideoPredictionResult
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.VideoViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

// Hardcoded colors from design
private val DarkBackground = Color(0xFF11222E)
private val CardBackground = Color(0xFF1A2A3A)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
private val ErrorRed = Color(0xFFFF6B6B)
private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun TestResultsScreen(
    resultId: String,
    navController: NavController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please log in to access test results", Toast.LENGTH_LONG).show()
            navController.navigate(Destinations.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val userId = currentUser.uid
    val viewModel: VideoViewModel = viewModel(
        factory = VideoViewModel.Factory(context, userId)
    )

    val testResults by viewModel.testResults.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadTestResults()
    }

    val testResult = testResults.find { it.id == resultId }

    if (testResult == null) {
        if (testResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ButtonBlue)
            }
            return
        }

        LaunchedEffect(Unit) {
            Toast.makeText(context, "Test result not found", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        return
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header Card - Keep gradient as is
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = if (testResult.seizureDetected) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFEF4444),
                                            Color(0xFFF87171)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF10B981),
                                            Color(0xFF34D399)
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (testResult.seizureDetected) R.drawable.ic_warning else R.drawable.ic_check
                                        ),
                                        contentDescription = "Result",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(12.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (testResult.seizureDetected) "Seizure Detected" else "No Seizure Detected",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Overall Confidence: ${(testResult.overallConfidence * 100).toInt()}%",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = testResult.videoName,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Analyzed: ${SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(testResult.analysisDate)}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Model Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Model Analysis",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Model Used:",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = testResult.modelName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Predictions Count:",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${testResult.predictions.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        if (testResult.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Column {
                                Text(
                                    text = "Notes:",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = testResult.notes,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Predictions
            if (testResult.predictions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Detailed Predictions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            testResult.predictions.take(5).forEach { prediction ->
                                PredictionItem(prediction = prediction)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (testResult.predictions.size > 5) {
                                Text(
                                    text = "... and ${testResult.predictions.size - 5} more predictions",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Destinations.VideoPlayer.createRoute(testResult.videoId))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ButtonBlue
                        )
                    ) {
                        Text("View Video")
                    }

                    Button(
                        onClick = {
                            navController.navigate(Destinations.TestLab.route)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonBlue,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Back to Test Lab")
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: VideoPredictionResult,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatTimestamp(prediction.timestamp),
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = prediction.predictedLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (prediction.predictedLabel.contains("seizure", ignoreCase = true)) {
                    ErrorRed
                } else {
                    SuccessGreen
                }
            )
        }

        Text(
            text = "${(prediction.confidence * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ButtonBlue
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatTimestamp(timestampMs: Long): String {
    val totalSeconds = timestampMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}