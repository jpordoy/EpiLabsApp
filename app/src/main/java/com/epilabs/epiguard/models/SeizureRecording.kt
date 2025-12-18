package com.epilabs.epiguard.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * SeizureRecording model for Firebase Firestore
 * Stores metadata about seizure detection events and video recordings
 */
data class SeizureRecording(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val detectionTimestamp: Long = System.currentTimeMillis(),
    val videoFilePath: String = "", // Local file path (temporary)
    val videoUrl: String = "", // Firebase Storage URL (permanent)
    val durationSeconds: Int = 0,
    val confidenceScore: Float = 0f,
    val seizureProbability: Float = 0f,
    val preRecordingSeconds: Int = 120, // seconds recorded before detection
    val postRecordingSeconds: Int = 120, // seconds recorded after detection
    val fileSize: Long = 0, // in bytes
    val notes: String = "",
    val isProcessing: Boolean = false, // true while video is being saved/uploaded
    @ServerTimestamp
    val createdAt: Date? = null,
    val metadata: Map<String, Any> = emptyMap() // Additional data (e.g., device info, session ID)
)