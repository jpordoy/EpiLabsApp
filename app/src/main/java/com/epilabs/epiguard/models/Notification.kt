package com.epilabs.epiguard.models

data class Notification(
    val id: Int = 0,
    val userID: Int,
    val contactID: Int = 0, // 0 = system message (no specific contact)
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

