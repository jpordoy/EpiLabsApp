// Updated VideoPlayerScreen.kt
package com.epilabs.epiguard.ui.screens.testlab

import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.epilabs.epiguard.models.VideoPredictionResult
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.VideoViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    navController: NavController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // FIXED: Check if user is authenticated
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please log in to access videos", Toast.LENGTH_LONG).show()
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

    val videos by viewModel.videos.collectAsState()
    val testResults by viewModel.testResults.collectAsState()

    // FIXED: Load data when screen starts
    LaunchedEffect(userId) {
        viewModel.loadVideos()
        viewModel.loadTestResults()
    }

    val video = remember(videos, videoId) {
        videos.find { it.id == videoId }
    }

    val videoTestResults = remember(testResults, videoId) {
        testResults.filter { it.videoId == videoId }
    }

    var currentPosition by remember { mutableLongStateOf(0L) }
    var currentPrediction by remember { mutableStateOf<VideoPredictionResult?>(null) }

    if (video == null) {
        // Show loading if videos are still loading
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        LaunchedEffect(Unit) {
            Toast.makeText(context, "Video not found", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        return
    }

    // Create ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoFile = File(video.localPath)
            if (videoFile.exists()) {
                val mediaItem = MediaItem.fromUri(videoFile.toUri())
                setMediaItem(mediaItem)
                prepare()
            } else {
                Toast.makeText(context, "Video file not found", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (exoPlayer.isCommandAvailable(ExoPlayer.COMMAND_PLAY_PAUSE)) {
                        exoPlayer.playWhenReady = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Update current prediction based on playback position
    LaunchedEffect(videoTestResults) {
        if (videoTestResults.isNotEmpty()) {
            while (true) {
                currentPosition = exoPlayer.currentPosition

                // Find the closest prediction to current playback position
                val allPredictions = videoTestResults.flatMap { it.predictions }
                val prediction = allPredictions.minByOrNull { result ->
                    abs(result.timestamp - currentPosition)
                }?.takeIf { result ->
                    abs(result.timestamp - currentPosition) <= 3000L // Within 3 seconds
                }

                currentPrediction = prediction
                delay(100) // Update every 100ms
            }
        }
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Video Player
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { playerView ->
                    playerView.player = exoPlayer
                }
            )

            // AI Prediction Overlay
            currentPrediction?.let { prediction ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (prediction.predictedLabel.contains("seizure", ignoreCase = true)) {
                                Color.Red.copy(alpha = 0.9f)
                            } else {
                                Color.Green.copy(alpha = 0.9f)
                            }
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "AI Detection",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = prediction.predictedLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Confidence: ${"%.1f".format(prediction.confidence * 100)}%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Video Info Overlay (Bottom)
            if (videoTestResults.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Analysis Available",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        videoTestResults.forEach { result ->
                            Text(
                                text = "${result.modelName}: ${if (result.seizureDetected) "Seizure Detected" else "No Seizure"}",
                                color = if (result.seizureDetected) Color.Red else Color.Green,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}