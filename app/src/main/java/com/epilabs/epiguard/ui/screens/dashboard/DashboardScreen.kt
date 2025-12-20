package com.epilabs.epiguard.ui.screens.dashboard

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.viewmodels.NotificationViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// Hardcoded colors from design
private val DarkBackground = Color(0xFF11222E)
private val TextFieldBorder = Color(0xFF2F414F)
private val ButtonBlue = Color(0xFF0163E1)
private val TextFieldPlaceholder = Color(0xFF606E77)
private val TextPrimary = Color(0xFFDECDCD)
private val TextSecondary = Color(0xFF8B9AA8)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentOrange = Color(0xFFFF9800)
private val AccentPurple = Color(0xFF9C27B0)
private val UnreadIndicator = Color(0xFF0163E1)

@Composable
fun DashboardScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val user by userViewModel.currentUser.collectAsState()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background image overlay
        Image(
            painter = painterResource(id = R.drawable.bg_23),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        Scaffold(
            topBar = { TopBar(navController) },
            bottomBar = { BottomNav(navController) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Hi ${user?.firstName ?: "User"},",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = TextSecondary,
                                            fontSize = 16.sp
                                        )
                                    ) { append("You have ") }

                                    withStyle(
                                        SpanStyle(
                                            color = Color(0xffd42a2a),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        append(
                                            "$unreadCount unread notification" +
                                                    if (unreadCount != 1) "s" else ""
                                        )
                                    }
                                    withStyle(
                                        SpanStyle(
                                            color = TextSecondary,
                                            fontSize = 16.sp
                                        )
                                    ) { append(" today") }
                                }
                            )
                        }
                    }
                }
                item {
                    // Quick actions
                    Text(
                        text = "Quick Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Start Detection",
                            icon = Icons.Default.Visibility,
                            iconColor = ButtonBlue,
                            modifier = Modifier.weight(1f)
                        ) {
                            navController.navigate(Destinations.SeizureDetection.route)
                        }

                        QuickActionCard(
                            title = "Contacts",
                            icon = Icons.Default.ContactPhone,
                            iconColor = AccentGreen,
                            modifier = Modifier.weight(1f)
                        ) {
                            navController.navigate(Destinations.Contacts.route)
                        }
                    }
                }

                // AI Test Lab Section
                item {
                    AITestLabCard(
                        userId = user?.userId ?: "",
                        navController = navController
                    )
                }

                item {
                    // Status overview
                    StatusOverviewCard(
                        unreadNotifications = unreadCount,
                        lastDetectionTime = "No recent activity"
                    )
                }

                item {
                    // Recent notifications
                    if (notifications.isNotEmpty()) {
                        Text(
                            text = "Recent Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                items(notifications.take(5)) { notification ->
                    NotificationItem(
                        title = notification.title,
                        message = notification.message,
                        timestamp = notification.timestamp,
                        isRead = notification.isRead,
                        onClick = { navController.navigate(Destinations.Notifications.route) }
                    )
                }

                if (notifications.size > 5) {
                    item {
                        Text(
                            text = "View all notifications",
                            fontSize = 15.sp,
                            color = ButtonBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Destinations.Notifications.route) }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextFieldBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun AITestLabCard(
    userId: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                try {
                    navController.navigate(Destinations.TestLab.createRoute(userId))
                } catch (e: IllegalArgumentException) {
                    Log.e("NavigationError", "Failed to navigate: ${e.message}")
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TextFieldBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Background image (fills entire card)
            Image(
                painter = painterResource(id = R.drawable.panel_background1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Optional overlay to keep text readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        DarkBackground.copy(alpha = 0.6f)
                    )
            )

            // Foreground content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI Test Lab",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Test your seizure videos with AI",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "AI Test Lab",
                    modifier = Modifier.size(40.dp),
                    tint = AccentPurple
                )
            }
        }
    }
}


@Composable
private fun StatusOverviewCard(
    unreadNotifications: Int,
    lastDetectionTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextFieldBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Status Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    label = "Unread Alerts",
                    value = unreadNotifications.toString(),
                    icon = Icons.Default.Notifications
                )
                StatusItem(
                    label = "Detection Status",
                    value = "Stopped",
                    icon = Icons.Default.Visibility
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Last Activity: $lastDetectionTime",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = ButtonBlue
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun NotificationItem(
    title: String,
    message: String,
    timestamp: Long,
    isRead: Boolean,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextFieldBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            UnreadIndicator,
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.padding(6.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (!isRead) FontWeight.Bold else FontWeight.Normal,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatter.format(timestamp),
                    fontSize = 12.sp,
                    color = TextFieldPlaceholder
                )
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun CompleteDashboardPreview() {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background image overlay
        Image(
            painter = painterResource(id = R.drawable.bg_23),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(horizontal = 15.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logos),
                        contentDescription = "App Logo",
                        modifier = Modifier.height(32.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = ButtonBlue) {
                                    Text(text = "3", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        ) {
                            IconButton(onClick = { }) {
                                Image(
                                    painter = painterResource(id = R.drawable.topbar_notification_bell),
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profile_image),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(34.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                BottomNav(navController = navController)
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 17.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "John Doe",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                item {
                    Text(
                        text = "Quick Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Start Detection",
                            icon = Icons.Default.Visibility,
                            iconColor = ButtonBlue,
                            modifier = Modifier.weight(1f)
                        ) { }

                        QuickActionCard(
                            title = "Contacts",
                            icon = Icons.Default.ContactPhone,
                            iconColor = AccentGreen,
                            modifier = Modifier.weight(1f)
                        ) { }
                    }
                }

                item {
                    AITestLabCard(userId = "preview-id", navController = navController)
                }

                item {
                    StatusOverviewCard(
                        unreadNotifications = 3,
                        lastDetectionTime = "2 hours ago"
                    )
                }

                item {
                    Text(
                        text = "Recent Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                items(3) { index ->
                    NotificationItem(
                        title = "Sample Notification ${index + 1}",
                        message = "This is a sample notification message for preview",
                        timestamp = System.currentTimeMillis() - (index * 3600000L),
                        isRead = index > 0,
                        onClick = { }
                    )
                }
            }
        }
    }
}