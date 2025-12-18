package com.epilabs.epiguard.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val userId: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val profileImageUrl: String = "",
    val gender: String = "",
    val dateOfBirth: Long = 0L,
    val deviceTokens: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    var isActive: Boolean = true,
    @ServerTimestamp
    val updatedAt: Date? = null
)

