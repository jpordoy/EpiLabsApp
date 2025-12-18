package com.epilabs.epiguard.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.epilabs.epiguard.utils.PreferencesHelper

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesHelper = PreferencesHelper(application)

    fun hasCompletedOnboarding(): Boolean {
        return preferencesHelper.hasCompletedOnboarding()
    }

    fun completeOnboarding() {
        preferencesHelper.setOnboardingCompleted(true)
    }
}