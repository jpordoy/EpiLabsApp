package com.epilabs.epiguard.ui.screens.preview

import com.google.firebase.firestore.ServerTimestamp
import java.util.Collections
import java.util.Date

data class PreviewUser(
    val userId: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val profileImageUrl: String = "",
    val deviceTokens: List<String> = Collections.emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    var isActive: Boolean = true, // Changed from val to var
    @ServerTimestamp
    val updatedAt: Date? = null
)