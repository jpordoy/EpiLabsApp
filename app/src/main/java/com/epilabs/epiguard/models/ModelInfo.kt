// ModelInfo.kt
package com.epilabs.epiguard.models

data class ModelInfo(
    val name: String,
    val description: String,
    val fileName: String,
    val version: String = "1.0",
    val accuracy: Float = 0f,
    val isActive: Boolean = true,
    val supportedFormats: List<String> = listOf("mp4", "mov", "avi")
)