package com.epilabs.epiguard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.epilabs.epiguard.data.repo.AuthRepository
import com.google.firebase.FirebaseApp

class EpiGuardApp : Application() {

    // Make AuthRepository available app-wide
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        FirebaseEmulatorHelper.setupEmulators()
        createNotificationChannels()

        // Initialize AuthRepository with application context
        authRepository = AuthRepository(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Seizure alerts channel
            val alertsChannel = NotificationChannel(
                "epiguard_alerts",
                "Seizure Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical seizure detection alerts"
                enableLights(true)
                enableVibration(true)
            }

            // General notifications channel
            val generalChannel = NotificationChannel(
                "epiguard_general",
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}