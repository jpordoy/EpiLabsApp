package com.epilabs.epiguard.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class VideoPredictionResult(
    val timestamp: Long = 0L, // milliseconds
    val predictedLabel: String = "",
    val confidence: Float = 0f,
    val frameNumber: Int = 0
)