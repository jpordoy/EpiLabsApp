package com.epilabs.epiguard.ui.screens.dashboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.epilabs.epiguard.EpiGuardApp
import com.epilabs.epiguard.R
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.nav.Destinations
import com.epilabs.epiguard.ui.theme.EpiGuardTheme
import com.epilabs.epiguard.ui.viewmodels.NotificationViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val user by userViewModel.currentUser.collectAsState()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = { TopBar(navController, showBackButton = false, userViewModel) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Welcome message
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user?.firstName ?: "User",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                // Quick actions
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Start Detection",
                        icon = Icons.Default.Visibility,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(Destinations.SeizureDetection.route)
                    }

                    QuickActionCard(
                        title = "Contacts",
                        icon = Icons.Default.ContactPhone,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate(Destinations.Contacts.route)
                    }
                }
            }

            // NEW AI Test Lab Section
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
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
            .height(100.dp)
            .clickable {
                try {
                    navController.navigate(Destinations.TestLab.createRoute(userId)) // Returns "testlab"
                } catch (e: IllegalArgumentException) {
                    Log.e("NavigationError", "Failed to navigate: ${e.message}")
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ){
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AI Test Lab",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Test your seizure videos with AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "AI Test Lab",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun StatusOverviewCard(
    unreadNotifications: Int,
    lastDetectionTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Last Activity: $lastDetectionTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.padding(4.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!isRead) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatter.format(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview(showBackground = true, apiLevel = 34)
@Composable
fun CompleteDashboardPreview() {
    EpiGuardTheme {
        // Create a preview NavController
        val navController = rememberNavController()

        Scaffold(
            topBar = {
                // Your actual TopBar component for preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // No back button for dashboard (showBackButton = false)
                    Box(modifier = Modifier.size(48.dp)) // Spacer when no back button

                    // Right side icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notification bell with badge
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(text = "3") // Preview with 3 notifications
                                }
                            }
                        ) {
                            IconButton(onClick = { }) {
                                Image(
                                    painter = painterResource(id = R.drawable.bell),
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                )
                            }
                        }

                        // Profile picture placeholder
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
                // Use the actual BottomNav component for preview
                BottomNav(navController = navController)
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Welcome message
                    Column {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "John Doe",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    // Quick actions
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Start Detection",
                            icon = Icons.Default.Visibility,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        ) { }

                        QuickActionCard(
                            title = "Contacts",
                            icon = Icons.Default.ContactPhone,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        ) { }
                    }
                }

                item {
                    // AI Test Lab
                    AITestLabCard(userId = "preview-id", navController = navController)
                }

                item {
                    // Status overview
                    StatusOverviewCard(
                        unreadNotifications = 3,
                        lastDetectionTime = "2 hours ago"
                    )
                }

                item {
                    // Recent notifications header
                    Text(
                        text = "Recent Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Sample notifications for preview
                items(3) { index ->
                    NotificationItem(
                        title = "Sample Notification ${index + 1}",
                        message = "This is a sample notification message for preview",
                        timestamp = System.currentTimeMillis() - (index * 3600000L), // 1 hour intervals
                        isRead = index > 0,
                        onClick = { }
                    )
                }
            }
        }
    }
}