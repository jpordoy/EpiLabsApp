package com.epilabs.epiguard.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing user preferences for seizure detection settings and app state
 */
class PreferencesHelper(context: Context) {
    companion object {
        private const val PREFS_NAME = "epilabs_seizure_settings"
        private const val KEY_CONFIDENCE_THRESHOLD = "confidence_threshold"
        private const val KEY_CONSECUTIVE_LIMIT = "consecutive_limit"
        private const val KEY_INFERENCE_INTERVAL = "inference_interval_ms"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // Default values
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.80f
        private const val DEFAULT_CONSECUTIVE_LIMIT = 3
        private const val DEFAULT_INFERENCE_INTERVAL = 5000L // 5 seconds
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Onboarding Methods
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    // Confidence Threshold (0.5 - 0.95)
    fun getConfidenceThreshold(): Float {
        return prefs.getFloat(KEY_CONFIDENCE_THRESHOLD, DEFAULT_CONFIDENCE_THRESHOLD)
    }

    fun setConfidenceThreshold(threshold: Float) {
        prefs.edit().putFloat(KEY_CONFIDENCE_THRESHOLD, threshold).apply()
    }

    // Consecutive Limit (1 - 8)
    fun getConsecutiveLimit(): Int {
        return prefs.getInt(KEY_CONSECUTIVE_LIMIT, DEFAULT_CONSECUTIVE_LIMIT)
    }

    fun setConsecutiveLimit(limit: Int) {
        prefs.edit().putInt(KEY_CONSECUTIVE_LIMIT, limit).apply()
    }

    // Inference Interval in milliseconds (2000 - 10000)
    fun getInferenceInterval(): Long {
        return prefs.getLong(KEY_INFERENCE_INTERVAL, DEFAULT_INFERENCE_INTERVAL)
    }

    fun setInferenceInterval(intervalMs: Long) {
        prefs.edit().putLong(KEY_INFERENCE_INTERVAL, intervalMs).apply()
    }

    // Get all settings as a data class for easy passing
    fun getAllSettings(): SeizureDetectionSettings {
        return SeizureDetectionSettings(
            confidenceThreshold = getConfidenceThreshold(),
            consecutiveLimit = getConsecutiveLimit(),
            inferenceIntervalMs = getInferenceInterval()
        )
    }

    // Reset seizure detection settings to defaults (keeps onboarding state)
    fun resetSeizureSettingsToDefaults() {
        prefs.edit()
            .remove(KEY_CONFIDENCE_THRESHOLD)
            .remove(KEY_CONSECUTIVE_LIMIT)
            .remove(KEY_INFERENCE_INTERVAL)
            .apply()
    }

    // Reset ALL preferences to defaults (including onboarding)
    fun resetAllToDefaults() {
        prefs.edit().clear().apply()
    }
}

/**
 * Data class to hold all seizure detection settings
 */
data class SeizureDetectionSettings(
    val confidenceThreshold: Float,
    val consecutiveLimit: Int,
    val inferenceIntervalMs: Long
) {
    // Helper to get total delay in seconds
    val totalDelaySeconds: Int
        get() = (consecutiveLimit * (inferenceIntervalMs / 1000)).toInt()
}