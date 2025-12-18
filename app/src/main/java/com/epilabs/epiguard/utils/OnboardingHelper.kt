// 6. OnboardingHelper.kt - Utility for managing onboarding state
package com.epilabs.epiguard.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object OnboardingHelper {
    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean = true) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ONBOARDING_COMPLETED, completed)
        }
    }

    fun resetOnboarding(context: Context) {
        setOnboardingCompleted(context, false)
    }
}