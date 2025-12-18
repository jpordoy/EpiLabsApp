package com.epilabs.epiguard.network

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Twilio API wrapper - calls Firebase Cloud Functions instead of direct Twilio
 * This keeps your Twilio credentials secure on the server side
 * Maintains the same API as your existing code expects
 */
class TwilioApi {
    private val functions: FirebaseFunctions = Firebase.functions

    suspend fun sendSmsBlocking(to: String, from: String, body: String): Boolean {
        return try {
            val data = hashMapOf(
                "to" to to,
                "from" to from,
                "body" to body
            )

            val result = functions
                .getHttpsCallable("sendTwilioSms")
                .call(data)
                .await()

            // Access getData() method instead of .data property
            val responseData = result.getData() as? Map<*, *>
            val isSuccess = responseData?.get("success") as? Boolean ?: false

            Log.d("TwilioApi", "SMS send result: $isSuccess")
            isSuccess
        } catch (e: Exception) {
            Log.e("TwilioApi", "Failed to send SMS", e)
            false
        }
    }
}