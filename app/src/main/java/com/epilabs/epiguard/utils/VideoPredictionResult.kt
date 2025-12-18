package com.epilabs.epiguard.utils

data class VideoPredictionResult(
    val timestamp: Long,
    val predictedLabel: String,
    val confidence: Float
)