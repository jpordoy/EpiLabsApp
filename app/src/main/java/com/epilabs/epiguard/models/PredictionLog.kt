package com.epilabs.epiguard.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PredictionLog(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val predictedLabel: String = "",
    val confidence: Float = 0f,
    val rawScores: List<Float> = emptyList(), // Changed from FloatArray for Firestore compatibility
    val frameNumber: Int = 0,
    val videoId: String = "", // Reference to video if applicable
    val sessionId: String = "", // Link to detection session
    @ServerTimestamp
    val createdAt: Date? = null
)