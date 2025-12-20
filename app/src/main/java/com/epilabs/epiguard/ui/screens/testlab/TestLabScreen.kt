package com.epilabs.epiguard.ui.screens.testlab

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.R
import com.epilabs.epiguard.models.ModelInfo
import com.epilabs.epiguard.models.TestResult
import com.epilabs.epiguard.models.VideoModel
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import com.epilabs.epiguard.ui.viewmodels.VideoViewModel
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
private val AccentGreen = Color(0xFF4CAF50)
private val AccentOrange = Color(0xFFFF9800)
private val ErrorRed = Color(0xFFFF6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestLabScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please log in to access Test Lab", Toast.LENGTH_LONG).show()
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
    val isLoading by viewModel.isLoading.collectAsState()
    val isClassifying by viewModel.isClassifying.collectAsState()
    val classificationProgress by viewModel.classificationProgress.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedVideo by remember { mutableStateOf<VideoModel?>(null) }
    var selectedModel by remember { mutableStateOf<ModelInfo?>(null) }
    var videoExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    var showModelInfo by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<VideoModel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadVideos()
        viewModel.loadTestResults()
    }

    val models = remember {
        listOf(
            ModelInfo(
                name = "SeizureNet v2.1",
                description = "Advanced deep learning model for seizure detection with 94% accuracy. Trained on diverse epilepsy patterns.",
                fileName = "model.tflite",
                version = "2.1",
                accuracy = 0.94f
            )
        )
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasStoragePermission = permissions.all { it.value }
        if (!hasStoragePermission) {
            Toast.makeText(context, "Storage permissions required", Toast.LENGTH_LONG).show()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadVideo(it) { success, errorMessage ->
                if (success) {
                    Toast.makeText(context, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMessage ?: "Upload failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = DarkBackground,
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
            // Header Section
            item {
                Text(
                    text = "AI Test Lab",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Upload Video Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Upload Video",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Select a seizure video for analysis",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                if (hasStoragePermission) {
                                    videoPickerLauncher.launch("video/*")
                                } else {
                                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                                    } else {
                                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                    permissionLauncher.launch(permissions)
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonBlue,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isLoading) "Uploading..." else "Choose Video File",
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Video Selection
            if (videos.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Select Video",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            ExposedDropdownMenuBox(
                                expanded = videoExpanded,
                                onExpandedChange = { videoExpanded = !videoExpanded }
                            ) {
                                TextField(
                                    value = selectedVideo?.name ?: "Choose a video",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = videoExpanded)
                                    },
                                    colors = TextFieldDefaults.colors(
                                        unfocusedContainerColor = DarkBackground,
                                        focusedContainerColor = DarkBackground,
                                        unfocusedIndicatorColor = TextFieldBorder,
                                        focusedIndicatorColor = ButtonBlue,
                                        unfocusedTextColor = TextPrimary,
                                        focusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = videoExpanded,
                                    onDismissRequest = { videoExpanded = false }
                                ) {
                                    videos.filter {
                                        it.status == VideoModel.VideoStatus.UPLOADED ||
                                                it.status == VideoModel.VideoStatus.ANALYZED
                                    }.forEach { video ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = video.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = "Status: ${video.status.name}",
                                                        fontSize = 12.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedVideo = video
                                                videoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Model Selection
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Select AI Model",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            if (selectedModel != null) {
                                IconButton(
                                    onClick = { showModelInfo = true }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_info),
                                        contentDescription = "Model Info",
                                        tint = ButtonBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = modelExpanded,
                            onExpandedChange = { modelExpanded = !modelExpanded }
                        ) {
                            TextField(
                                value = selectedModel?.name ?: "Choose an AI model",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = DarkBackground,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedIndicatorColor = TextFieldBorder,
                                    focusedIndicatorColor = ButtonBlue,
                                    unfocusedTextColor = TextPrimary,
                                    focusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = modelExpanded,
                                onDismissRequest = { modelExpanded = false }
                            ) {
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = model.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Accuracy: ${(model.accuracy * 100).toInt()}%",
                                                    fontSize = 12.sp,
                                                    color = AccentGreen
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedModel = model
                                            modelExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Analysis Button
            if (selectedVideo != null && selectedModel != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isClassifying) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Analyzing Video...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    LinearProgressIndicator(
                                        progress = { classificationProgress / 100f },
                                        color = ButtonBlue,
                                        trackColor = TextFieldBorder,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "$classificationProgress% complete",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.classifyVideo(
                                            video = selectedVideo!!,
                                            modelName = selectedModel!!.name
                                        ) { success, result, error ->
                                            if (success && result != null) {
                                                Toast.makeText(
                                                    context,
                                                    "Analysis completed successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate(Destinations.TestResults.createRoute(result.id))
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    error ?: "Analysis failed",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentGreen,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_brain),
                                        contentDescription = "Analyze",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Start AI Analysis",
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // My Videos Section
            if (videos.isNotEmpty()) {
                item {
                    Text(
                        text = "My Videos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                items(videos) { video ->
                    VideoItemCard(
                        video = video,
                        testResults = testResults.filter { it.videoId == video.id },
                        onPlayClick = {
                            navController.navigate(Destinations.VideoPlayer.createRoute(video.id))
                        },
                        onDeleteClick = {
                            showDeleteDialog = video
                        },
                        onViewResultsClick = { resultId ->
                            navController.navigate(Destinations.TestResults.createRoute(resultId))
                        }
                    )
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_video),
                                contentDescription = "No videos",
                                tint = TextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "No Videos Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Upload your first seizure video to start AI analysis",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Model Info Dialog
    if (showModelInfo && selectedModel != null) {
        AlertDialog(
            onDismissRequest = { showModelInfo = false },
            title = {
                Text(
                    text = selectedModel!!.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = selectedModel!!.description,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Version:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = selectedModel!!.version,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Accuracy:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(selectedModel!!.accuracy * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = AccentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showModelInfo = false }
                ) {
                    Text("OK", color = ButtonBlue)
                }
            },
            containerColor = CardBackground
        )
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { video ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "Delete Video",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${video.name}? This action cannot be undone.",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVideo(video.id) { success, error ->
                            if (success) {
                                Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, error ?: "Delete failed", Toast.LENGTH_LONG).show()
                            }
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Profile editing functionality will be implemented here.",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("OK", color = ButtonBlue)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
fun VideoItemCard(
    video: VideoModel,
    testResults: List<TestResult>,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewResultsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Uploaded: ${
                            SimpleDateFormat(
                                "MMM dd, yyyy",
                                Locale.getDefault()
                            ).format(video.createdAt)
                        }",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = when (video.status) {
                                VideoModel.VideoStatus.UPLOADED -> ButtonBlue.copy(alpha = 0.2f)
                                VideoModel.VideoStatus.PROCESSING -> AccentOrange.copy(alpha = 0.2f)
                                VideoModel.VideoStatus.ANALYZED -> AccentGreen.copy(alpha = 0.2f)
                                VideoModel.VideoStatus.ERROR -> Color(0xFFFF6B6B).copy(alpha = 0.2f)
                                else -> TextFieldBorder.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = video.status.name,
                        fontSize = 12.sp,
                        color = when (video.status) {
                            VideoModel.VideoStatus.UPLOADED -> ButtonBlue
                            VideoModel.VideoStatus.PROCESSING -> AccentOrange
                            VideoModel.VideoStatus.ANALYZED -> AccentGreen
                            VideoModel.VideoStatus.ERROR -> Color(0xFFFF6B6B)
                            else -> TextSecondary
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (testResults.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Analysis Results:",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    testResults.take(2).forEach { result ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = result.modelName,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${(result.overallConfidence * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    color = if (result.seizureDetected) Color(0xFFFF6B6B) else AccentGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = { onViewResultsClick(result.id) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ButtonBlue
                                    )
                                ) {
                                    Text(
                                        text = "View",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onPlayClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ButtonBlue
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "Play",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play")
                }

                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = ErrorRed
                    )
                }
            }
        }
    }
}