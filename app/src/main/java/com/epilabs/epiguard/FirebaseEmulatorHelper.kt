package com.epilabs.epiguard

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.FirebaseFunctions

object FirebaseEmulatorHelper {
    private const val USE_EMULATOR = true  // Set to false for production

    // For Android Emulator use: 10.0.2.2
    // For Physical Device use: Your computer's IP
    private const val EMULATOR_HOST = "10.0.2.2"  // Change if using physical device

    private const val AUTH_PORT = 9099
    private const val FIRESTORE_PORT = 8081  // UPDATED from 8080
    private const val FUNCTIONS_PORT = 5001

    fun setupEmulators() {
        if (USE_EMULATOR) {
            FirebaseAuth.getInstance().useEmulator(EMULATOR_HOST, AUTH_PORT)

            val settings = FirebaseFirestoreSettings.Builder()
                .setHost("$EMULATOR_HOST:$FIRESTORE_PORT")
                .setSslEnabled(false)
                .setPersistenceEnabled(false)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings

            FirebaseFunctions.getInstance().useEmulator(EMULATOR_HOST, FUNCTIONS_PORT)

            Log.d("Firebase", "🔧 Using Firebase Emulators")
        } else {
            Log.d("Firebase", "🌐 Using Production Firebase")
        }
    }
}