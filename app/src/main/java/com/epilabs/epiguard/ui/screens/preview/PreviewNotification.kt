package com.epilabs.epiguard.ui.screens.preview

// Fake Notification model (adapt shape to your real Notification class)
data class PreviewNotification(
    val userID: Int,
    val contactID: Int,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean
)