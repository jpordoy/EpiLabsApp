package com.epilabs.epiguard.ui.screens.detection

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.data.repo.NotificationRepository
import com.epilabs.epiguard.data.repo.PredictionLogRepository
import com.epilabs.epiguard.network.TwilioApi
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.viewmodels.NotificationViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.utils.TFLiteHelper
import com.epilabs.epiguard.utils.WakeLockManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

// Exact colors from the design
private val DarkBackground = Color(0xFF11222E)
private val CardBackground = Color(0xFF1A2A3A)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
private val FacebookGreen = Color(0xFF34C759)
private val VibrantRed = Color(0xFFFF3B30)

@OptIn(ExperimentalGetImage::class)
@Composable
fun LocalSeizureDetectorScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userId = currentUser?.userId ?: ""
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

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

    // Initialize repositories
    val notificationRepository = remember { NotificationRepository() }
    val predictionLogRepository = remember { PredictionLogRepository() }
    val tfliteHelper = remember {
        TFLiteHelper(
            context = context,
            predictionLogRepository = predictionLogRepository
        )
    }
    val twilioApi = remember { TwilioApi() }
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Detection state
    var isDetecting by remember { mutableStateOf(false) }
    var predictionText by remember { mutableStateOf("Ready to start detection") }
    var lastAnalyzedTime by remember { mutableStateOf(0L) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }
    var detectionStatus by remember { mutableStateOf("Stopped") }
    var alertStatus by remember { mutableStateOf("No alerts sent") }
    var frameCount by remember { mutableStateOf(0) }
    var sessionStartTime by remember { mutableStateOf<Long?>(null) }
    var lastPredictionTime by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf(0f) }
    var seizureProbability by remember { mutableStateOf(0f) }
    var notSeizureProbability by remember { mutableStateOf(0f) }
    var framesProcessed by remember { mutableStateOf(0) }
    var detectionSessionId by remember { mutableStateOf("") }

    // Seizure detection logic
    var consecutiveSeizures by remember { mutableStateOf(0) }
    var contactNumber by remember { mutableStateOf("+1234567890") }
    var twilioFromNumber by remember { mutableStateOf("+1234567890") }

    val analysisIntervalMs = 100L
    val seizureConfidenceThreshold = 0.75f
    val consecutiveLimit = 3

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            detectionStatus = "Camera permission required for seizure detection"
        }
    }

    val wakeLockManager = remember { WakeLockManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            wakeLockManager.cleanup()
            tfliteHelper.close()
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (!hasCameraPermission) {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ButtonBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "EpiGuard needs camera access to monitor for seizures",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Grant Permission", color = Color.White)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Local Camera Detection",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Configuration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it },
                            label = { Text("Emergency Contact Number", color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isDetecting,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ButtonBlue,
                                unfocusedBorderColor = TextFieldBorder,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = ButtonBlue,
                                disabledBorderColor = TextFieldBorder,
                                disabledContainerColor = DarkBackground,
                                disabledTextColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (!isDetecting) {
                                isDetecting = true
                                detectionStatus = "Starting detection..."
                                sessionStartTime = System.currentTimeMillis()
                                detectionSessionId = sessionStartTime.toString()
                                frameCount = 0
                                framesProcessed = 0
                                consecutiveSeizures = 0
                                predictionText = "Waiting for 10 frames..."
                                alertStatus = "Monitoring active"
                                wakeLockManager.acquireWakeLock()
                                (context as? Activity)?.let { activity ->
                                    wakeLockManager.keepScreenOn(activity, true)
                                }
                                coroutineScope.launch {
                                    notificationRepository.sendDetectionStartedNotification("Local Camera")
                                    notificationViewModel.loadNotifications()
                                    notificationViewModel.loadUnreadCount()
                                }
                            } else {
                                isDetecting = false
                                detectionStatus = "Detection stopped"
                                predictionText = "Detection stopped"
                                val durationMs = sessionStartTime?.let { System.currentTimeMillis() - it } ?: 0L
                                sessionStartTime = null
                                alertStatus = "No alerts sent"
                                frameCount = 0
                                framesProcessed = 0
                                confidence = 0f
                                seizureProbability = 0f
                                notSeizureProbability = 0f
                                consecutiveSeizures = 0
                                wakeLockManager.releaseWakeLock()
                                (context as? Activity)?.let { activity ->
                                    wakeLockManager.keepScreenOn(activity, false)
                                }
                                coroutineScope.launch {
                                    notificationRepository.sendDetectionStoppedNotification(durationMs, detectionSessionId)
                                    notificationViewModel.loadNotifications()
                                    notificationViewModel.loadUnreadCount()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDetecting) VibrantRed else FacebookGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isDetecting) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isDetecting) "Stop Detection" else "Start Detection",
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            } else {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }
                        },
                        modifier = Modifier
                            .background(
                                ButtonBlue.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = ButtonBlue
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            detectionStatus.contains("Error") -> VibrantRed.copy(alpha = 0.2f)
                            detectionStatus.contains("SEIZURE") -> VibrantRed.copy(alpha = 0.3f)
                            isDetecting -> FacebookGreen.copy(alpha = 0.2f)
                            else -> CardBackground
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status: $detectionStatus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Last Prediction: $predictionText",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Confidence: ${String.format("%.2f", confidence)}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Frames Processed: $framesProcessed",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Seizure Probability: ${String.format("%.2f%%", seizureProbability * 100)}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Not Seizure Probability: ${String.format("%.2f%%", notSeizureProbability * 100)}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        if (sessionStartTime != null) {
                            val duration = (System.currentTimeMillis() - sessionStartTime!!) / 1000
                            Text(
                                text = "Session Duration: ${duration}s",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "Alert Status: $alertStatus",
                            fontSize = 13.sp,
                            color = TextFieldPlaceholder
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = {
                                PreviewView(context).apply {
                                    id = android.view.View.generateViewId()
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { previewView ->
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                    val imageAnalyzer = ImageAnalysis.Builder()
                                        .setTargetResolution(android.util.Size(224, 224))
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                                if (!isDetecting) {
                                                    imageProxy.close()
                                                    return@setAnalyzer
                                                }

                                                val currentTime = System.currentTimeMillis()
                                                if (currentTime - lastAnalyzedTime < analysisIntervalMs) {
                                                    imageProxy.close()
                                                    return@setAnalyzer
                                                }
                                                lastAnalyzedTime = currentTime
                                                frameCount++

                                                val image = imageProxy.image ?: run {
                                                    imageProxy.close()
                                                    return@setAnalyzer
                                                }

                                                val bitmap = imageToBitmap(image, imageProxy.imageInfo.rotationDegrees)
                                                val result = tfliteHelper.addFrameAndPredict(
                                                    bitmap,
                                                    userId,
                                                    sessionId = detectionSessionId,
                                                    videoId = ""
                                                )

                                                result?.let { predictions ->
                                                    framesProcessed++
                                                    notSeizureProbability = predictions[0]
                                                    seizureProbability = predictions[1]

                                                    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                                    lastPredictionTime = formatter.format(currentTime)

                                                    val predictedLabel = if (seizureProbability > notSeizureProbability) "Seizure" else "Not Seizure"
                                                    confidence = if (seizureProbability > notSeizureProbability) seizureProbability else notSeizureProbability

                                                    predictionText = "$predictedLabel ($lastPredictionTime)"
                                                    detectionStatus = "Processing frames... ($frameCount total)"

                                                    if (predictedLabel == "Seizure" && confidence >= seizureConfidenceThreshold) {
                                                        consecutiveSeizures++
                                                        if (consecutiveSeizures >= consecutiveLimit) {
                                                            consecutiveSeizures = 0

                                                            coroutineScope.launch {
                                                                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                                                val timestampRange = "${formatter.format(currentTime - 5000)} - ${formatter.format(currentTime)}"
                                                                val message = "Seizure detected ($timestampRange). Please check immediately."

                                                                notificationRepository.sendSeizureDetectedNotification(timestampRange, message)
                                                                notificationViewModel.loadNotifications()
                                                                notificationViewModel.loadUnreadCount()

                                                                try {
                                                                    twilioApi.sendSmsBlocking(
                                                                        to = contactNumber,
                                                                        from = twilioFromNumber,
                                                                        body = message
                                                                    )
                                                                } catch (e: Exception) {
                                                                    Log.e("LocalDetector", "SMS failed: ${e.message}")
                                                                }

                                                                alertStatus = "Seizure detected! Alerts sent"
                                                                detectionStatus = "⚠️ SEIZURE ALERT - Check patient immediately"
                                                            }
                                                        }
                                                    } else {
                                                        consecutiveSeizures = 0

                                                        if (seizureProbability > 0.5f) {
                                                            alertStatus = "Elevated seizure probability detected"
                                                            detectionStatus = "Monitoring - elevated risk detected"
                                                        } else {
                                                            alertStatus = "Normal monitoring - no alerts"
                                                            detectionStatus = "Active monitoring"
                                                        }
                                                    }
                                                } ?: run {
                                                    predictionText = "Collecting frames... ($frameCount/10)"
                                                    detectionStatus = "Waiting for sufficient frames"
                                                }

                                                imageProxy.close()
                                            }
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalyzer
                                        )
                                    } catch (exc: Exception) {
                                        detectionStatus = "Camera error: ${exc.message}"
                                    }
                                }, ContextCompat.getMainExecutor(context))
                            }
                        )

                        if (isDetecting) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        VibrantRed,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "🔴 LIVE",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Detection Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "• Uses 10-frame sequences for analysis",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• Processes at ~10 FPS maximum",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• AI model trained on seizure detection",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• Real-time probability scoring",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• Automatic notifications to contacts and dashboard",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• SMS alerts via Twilio for seizure detection",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun imageToBitmap(image: Image, rotationDegrees: Int): Bitmap {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val jpegBytes = out.toByteArray()
    val bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
}