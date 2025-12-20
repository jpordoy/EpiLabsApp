package com.epilabs.epiguard.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing user preferences for seizure detection settings,
 * onboarding state, and user session persistence
 */
class PreferencesHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "epilabs_seizure_settings"

        // Seizure detection keys
        private const val KEY_CONFIDENCE_THRESHOLD = "confidence_threshold"
        private const val KEY_CONSECUTIVE_LIMIT = "consecutive_limit"
        private const val KEY_INFERENCE_INTERVAL = "inference_interval_ms"

        // App state keys
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // 🔐 Session keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"

        // Default values
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.80f
        private const val DEFAULT_CONSECUTIVE_LIMIT = 3
        private const val DEFAULT_INFERENCE_INTERVAL = 5000L // 5 seconds
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    // ─────────────────────────────────────────────
    // 🔐 SESSION METHODS (NEW)
    // ─────────────────────────────────────────────

    fun saveUserSession(userId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun clearUserSession() {
        prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_ID)
            .apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getLoggedInUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    // ─────────────────────────────────────────────
    // 🚀 ONBOARDING METHODS
    // ─────────────────────────────────────────────

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    // ─────────────────────────────────────────────
    // ⚙️ SEIZURE DETECTION SETTINGS
    // ─────────────────────────────────────────────

    fun getConfidenceThreshold(): Float {
        return prefs.getFloat(KEY_CONFIDENCE_THRESHOLD, DEFAULT_CONFIDENCE_THRESHOLD)
    }

    fun setConfidenceThreshold(threshold: Float) {
        prefs.edit().putFloat(KEY_CONFIDENCE_THRESHOLD, threshold).apply()
    }

    fun getConsecutiveLimit(): Int {
        return prefs.getInt(KEY_CONSECUTIVE_LIMIT, DEFAULT_CONSECUTIVE_LIMIT)
    }

    fun setConsecutiveLimit(limit: Int) {
        prefs.edit().putInt(KEY_CONSECUTIVE_LIMIT, limit).apply()
    }

    fun getInferenceInterval(): Long {
        return prefs.getLong(KEY_INFERENCE_INTERVAL, DEFAULT_INFERENCE_INTERVAL)
    }

    fun setInferenceInterval(intervalMs: Long) {
        prefs.edit().putLong(KEY_INFERENCE_INTERVAL, intervalMs).apply()
    }

    // ─────────────────────────────────────────────
    // 📦 HELPERS
    // ─────────────────────────────────────────────

    fun getAllSettings(): SeizureDetectionSettings {
        return SeizureDetectionSettings(
            confidenceThreshold = getConfidenceThreshold(),
            consecutiveLimit = getConsecutiveLimit(),
            inferenceIntervalMs = getInferenceInterval()
        )
    }

    fun resetSeizureSettingsToDefaults() {
        prefs.edit()
            .remove(KEY_CONFIDENCE_THRESHOLD)
            .remove(KEY_CONSECUTIVE_LIMIT)
            .remove(KEY_INFERENCE_INTERVAL)
            .apply()
    }

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
    val totalDelaySeconds: Int
        get() = (consecutiveLimit * (inferenceIntervalMs / 1000)).toInt()
}
