package com.epilabs.epiguard.ui.screens.detection

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.UserViewModel

// Exact colors from the design
private val DarkBackground = Color(0xFF11222E)
private val CardBackground = Color(0xFF1A2A3A)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
private val FacebookGreen = Color(0xFF42B72A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionScreen(navController: NavController) {
    val userViewModel: UserViewModel = viewModel()

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
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Choose Detection Device",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Select how you want to monitor for seizures",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    DeviceOptionCard(
                        icon = Icons.Default.CameraAlt,
                        title = "Basic Camera Detection",
                        description = "Use this phone's built-in camera for basic seizure detection",
                        onClick = {
                            try {
                                navController.navigate(Destinations.LocalSeizureDetection.route)
                            } catch (e: IllegalArgumentException) {
                                Log.e("NavigationError", "Failed to navigate to local_seizure_detection: ${e.message}")
                            }
                        },
                        isRecommended = true
                    )
                }

                item {
                    DeviceOptionCard(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Connect to Mobile Phone",
                        description = "Connect to another phone running a camera server app",
                        onClick = {
                            try {
                                navController.navigate(Destinations.SeizureDetection.route)
                            } catch (e: IllegalArgumentException) {
                                Log.e("NavigationError", "Failed to navigate to seizure_detection: ${e.message}")
                            }
                        }
                    )
                }

                item {
                    DeviceOptionCard(
                        icon = Icons.Default.Videocam,
                        title = "Wireless Camera",
                        description = "Connect to a wireless camera or baby monitor",
                        onClick = {
                            try {
                                navController.navigate(Destinations.WirelessCameraDetection.route)
                            } catch (e: IllegalArgumentException) {
                                Log.e("NavigationError", "Failed to navigate to wireless_camera_detection: ${e.message}")
                            }
                        },
                        isComingSoon = true
                    )
                }
            }

            // Back Button
            Button(
                onClick = {
                    try {
                        navController.navigate(Destinations.Dashboard.route)
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationError", "Failed to navigate to dashboard: ${e.message}")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Back to Dashboard",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun DeviceOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isComingSoon: Boolean = false,
    isRecommended: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isComingSoon) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecommended) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isRecommended -> ButtonBlue.copy(alpha = 0.2f)
                isComingSoon -> CardBackground.copy(alpha = 0.6f)
                else -> CardBackground
            }
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            // Recommended badge
            if (isRecommended) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FacebookGreen)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RECOMMENDED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isRecommended -> FacebookGreen
                                isComingSoon -> TextFieldBorder
                                else -> ButtonBlue
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isComingSoon) TextSecondary else TextPrimary
                        )

                        if (isComingSoon) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coming Soon",
                                fontSize = 12.sp,
                                color = ButtonBlue,
                                modifier = Modifier
                                    .background(
                                        ButtonBlue.copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Arrow Icon
                if (!isComingSoon) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go to option",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}