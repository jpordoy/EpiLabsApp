package com.epilabs.epiguard.ui.screens.detection

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.data.repo.NotificationRepository
import com.epilabs.epiguard.data.repo.PredictionLogRepository
import com.epilabs.epiguard.seizure.IpMjpegDetector
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.viewmodels.NotificationViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import kotlin.text.*

// Custom Color Palette
object CustomColors {
    val Background = Color(0xFF11222E)
    val TextFieldBackground = Color(0xFF11222E)
    val TextFieldBorder = Color(0xFF2F414F)
    val PlaceholderText = Color(0xFF606E77)
    val PrimaryText = Color(0xFFDECDCD)
    val ButtonBlue = Color(0xFF0163E1)
    val ButtonBlueDark = Color(0xFF0C5AC7)
    val ForgottenPasswordText = Color(0xFFF4E9F6)
    val FacebookGreen = Color(0xFF42B72A)
    val ErrorRed = Color(0xFFFF6B6B)
    val WarningOrange = Color(0xFFFF9E3A)
}

@SuppressLint("DefaultLocale")
@Composable
fun SeizureDetectionScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationRepository = remember { NotificationRepository() }
    val predictionLogRepository = remember { PredictionLogRepository() }

    // Get current user - THIS IS THE KEY CHANGE
    val currentUser by userViewModel.currentUser.collectAsState()
    val userId = currentUser?.userId ?: "" // Get Firebase UID as String

    var isDetecting by remember { mutableStateOf(false) }
    var streamUrl by remember { mutableStateOf("http://192.168.0.128:8080/video") }
    var contactNumber by remember { mutableStateOf("+1234567890") }
    var currentFrame by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var lastPrediction by remember { mutableStateOf("No prediction yet") }
    var confidence by remember { mutableFloatStateOf(0f) }
    var streamStatus by remember { mutableStateOf("Stopped") }
    var alertStatus by remember { mutableStateOf("No alerts sent") }
    var detector by remember { mutableStateOf<IpMjpegDetector?>(null) }
    var detectionSessionId by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            detector?.stop()
        }
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = CustomColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CustomColors.Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Seizure Detection",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CustomColors.PrimaryText
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CustomColors.TextFieldBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CustomColors.PrimaryText
                    )

                    OutlinedTextField(
                        value = streamUrl,
                        onValueChange = { streamUrl = it },
                        label = { Text("Camera Stream URL", color = CustomColors.PlaceholderText) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDetecting,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CustomColors.ButtonBlue,
                            unfocusedBorderColor = CustomColors.TextFieldBorder,
                            focusedTextColor = CustomColors.PrimaryText,
                            unfocusedTextColor = CustomColors.PrimaryText,
                            disabledTextColor = CustomColors.PlaceholderText,
                            focusedLabelColor = CustomColors.ButtonBlue,
                            unfocusedLabelColor = CustomColors.PlaceholderText
                        ),
                        textStyle = TextStyle(color = CustomColors.PrimaryText)
                    )

                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Emergency Contact Number", color = CustomColors.PlaceholderText) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDetecting,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CustomColors.ButtonBlue,
                            unfocusedBorderColor = CustomColors.TextFieldBorder,
                            focusedTextColor = CustomColors.PrimaryText,
                            unfocusedTextColor = CustomColors.PrimaryText,
                            disabledTextColor = CustomColors.PlaceholderText,
                            focusedLabelColor = CustomColors.ButtonBlue,
                            unfocusedLabelColor = CustomColors.PlaceholderText
                        ),
                        textStyle = TextStyle(color = CustomColors.PrimaryText)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (!isDetecting) {
                            // CRITICAL: Check if user is logged in
                            if (userId.isEmpty()) {
                                alertStatus = "Error: User not logged in"
                                return@Button
                            }

                            val sessionId = System.currentTimeMillis().toString()
                            detectionSessionId = sessionId

                            // Create detector with STRING userId
                            detector = IpMjpegDetector(
                                context = context,
                                userId = userId, // Pass Firebase UID (String)
                                streamUrl = streamUrl,
                                twilioToNumber = contactNumber,
                                twilioFromNumber = "+1234567890",
                                notificationRepository = notificationRepository,
                                predictionLogRepository = predictionLogRepository,
                                onFrame = { bitmap -> currentFrame = bitmap },
                                onPrediction = { timestamp, label, conf ->
                                    lastPrediction = "$label ($timestamp)"
                                    confidence = conf
                                },
                                onSeizureDetected = { timestamp, message ->
                                    alertStatus = "Seizure detected! Alerts sent"
                                    coroutineScope.launch {
                                        notificationViewModel.loadNotifications()
                                        notificationViewModel.loadUnreadCount()
                                    }
                                },
                                onStreamStatus = { status -> streamStatus = status },
                                onDetectionStarted = {
                                    coroutineScope.launch {
                                        notificationViewModel.loadNotifications()
                                        notificationViewModel.loadUnreadCount()
                                    }
                                },
                                onDetectionStopped = { durationMs ->
                                    coroutineScope.launch {
                                        notificationViewModel.loadNotifications()
                                        notificationViewModel.loadUnreadCount()
                                    }
                                }
                            )
                            detector?.start()
                            isDetecting = true
                        } else {
                            detector?.stop()
                            detector = null
                            isDetecting = false
                            currentFrame = null
                            lastPrediction = "Detection stopped"
                            confidence = 0f
                            streamStatus = "Stopped"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors.ButtonBlue
                    )
                ) {
                    Icon(
                        imageVector = if (isDetecting) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(if (isDetecting) "Stop Detection" else "Start Detection", color = Color.White)
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        streamStatus.contains("Error") -> CustomColors.ErrorRed.copy(alpha = 0.2f)
                        isDetecting -> CustomColors.ButtonBlue.copy(alpha = 0.1f)
                        else -> CustomColors.TextFieldBorder.copy(alpha = 0.5f)
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status: $streamStatus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CustomColors.PrimaryText
                    )
                    Text(
                        text = "Last Prediction: $lastPrediction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CustomColors.PrimaryText
                    )
                    Text(
                        text = "Confidence: ${String.format("%.2f", confidence)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CustomColors.PrimaryText
                    )
                    Text(
                        text = "Alert Status: $alertStatus",
                        style = MaterialTheme.typography.bodySmall,
                        color = CustomColors.PlaceholderText
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = CustomColors.TextFieldBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentFrame != null && !currentFrame!!.isRecycled) {
                        Image(
                            bitmap = currentFrame!!.asImageBitmap(),
                            contentDescription = "Live feed",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        CustomColors.TextFieldBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDetecting) "Connecting..." else "No video feed",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CustomColors.PlaceholderText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}