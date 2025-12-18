package com.epilabs.epiguard.models

import java.util.Collections.emptyMap

data class AppNotification(
    val notificationId: String = "",
    val userId: String = "",
    val contactId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SEIZURE_ALERT", // SEIZURE_ALERT, CONTACT_REQUEST, etc.
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false, // Changed to var
    val metadata: Map<String, Any> = emptyMap()
)
