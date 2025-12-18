package com.epilabs.epiguard

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    fun triggerSeizureAlert(confidence: Float) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("FirebaseManager", "User not authenticated")
            return
        }

        Log.d("SeizureAlert", "🚨 Triggering seizure alert with confidence: $confidence")

        val data = hashMapOf(
            "userId" to currentUser.uid,
            "timestamp" to System.currentTimeMillis(),
            "confidence" to confidence
        )

        functions
            .getHttpsCallable("handleSeizureDetection")
            .call(data)
            .addOnSuccessListener { result ->
                val responseData = result.getData() as? Map<*, *>
                val contactsNotified = responseData?.get("contactsNotified") as? Long ?: 0
                val isEmulator = responseData?.get("isEmulator") as? Boolean ?: false

                Log.d("SeizureAlert", "✅ Alert sent successfully to $contactsNotified contacts")
                if (isEmulator) {
                    Log.d("SeizureAlert", "🔧 Running on emulator - SMS messages are mocked")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SeizureAlert", "❌ Failed to send alert", exception)
            }
    }

    fun testEmulator() {
        functions
            .getHttpsCallable("testEmulator")
            .call()
            .addOnSuccessListener { result ->
                Log.d("EmulatorTest", "✅ Emulator test successful: ${result.getData()}")
            }
            .addOnFailureListener { exception ->
                Log.e("EmulatorTest", "❌ Emulator test failed", exception)
            }
    }
}