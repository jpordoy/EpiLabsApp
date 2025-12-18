package com.epilabs.epiguard.ui.screens.notifications

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.epilabs.epiguard.models.AppNotification
import com.epilabs.epiguard.ui.components.BottomNav
import com.epilabs.epiguard.ui.components.ConfirmDialog
import com.epilabs.epiguard.ui.components.ErrorDialog
import com.epilabs.epiguard.ui.components.LoadingDialog
import com.epilabs.epiguard.ui.components.TopBar
import com.epilabs.epiguard.ui.viewmodels.NotificationViewModel
import com.epilabs.epiguard.ui.viewmodels.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NotificationsScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()
    val error by notificationViewModel.error.collectAsState()

    var selectedNotification by remember { mutableStateOf<AppNotification?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(navController, userViewModel = userViewModel) },
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty() && !isLoading) {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "You'll see seizure alerts and other important notifications here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    notificationViewModel.markAsRead(notification.notificationId)
                                }
                            },
                            onDelete = {
                                selectedNotification = notification
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    ConfirmDialog(
        isVisible = showDeleteDialog,
        title = "Delete Notification",
        message = "Are you sure you want to delete this notification?",
        onConfirm = {
            selectedNotification?.let { notification ->
                notificationViewModel.deleteNotification(notification.notificationId)
            }
            showDeleteDialog = false
            selectedNotification = null
        },
        onDismiss = {
            showDeleteDialog = false
            selectedNotification = null
        },
        confirmText = "Delete"
    )

    // Loading and error dialogs
    LoadingDialog(
        isVisible = isLoading,
        message = "Loading notifications..."
    )

    ErrorDialog(
        isVisible = error != null,
        message = error ?: "",
        onDismiss = { notificationViewModel.clearError() }
    )
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.padding(6.dp))
            }

            // Notification type icon
            val (iconColor, bgColor) = when (notification.type) {
                "SEIZURE_ALERT" -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatter.format(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}