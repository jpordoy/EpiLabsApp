package com.epilabs.epiguard.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TestResult(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val videoName: String = "",
    val modelName: String = "",
    val predictions: List<com.epilabs.epiguard.models.VideoPredictionResult> = emptyList(),
    val overallConfidence: Float = 0f,
    val seizureDetected: Boolean = false,
    val analysisDate: Date = Date(),
    @ServerTimestamp
    val createdAt: Date? = null,
    val notes: String = ""
)