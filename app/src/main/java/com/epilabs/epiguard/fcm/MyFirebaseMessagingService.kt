package com.epilabs.epiguard.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.epilabs.epiguard.MainActivity
import com.epilabs.epiguard.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received: ${remoteMessage.data}")

        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "EpiGuard Alert"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "New notification"
        val type = remoteMessage.data["type"] ?: "GENERAL"

        showNotification(title, body, type)

        // Save to Firestore notifications collection
        saveNotificationToFirestore(title, body, type, remoteMessage.data)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token: $token")

        // Update user's device token in Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            updateDeviceToken(currentUser.uid, token)
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "epiguard_alerts"

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "EpiGuard Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Seizure detection alerts and notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun saveNotificationToFirestore(title: String, body: String, type: String, data: Map<String, String>) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val notification = hashMapOf(
            "userId" to currentUser.uid,
            "title" to title,
            "message" to body,
            "type" to type,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false,
            "metadata" to data
        )

        FirebaseFirestore.getInstance()
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener { Log.d("FCM", "Notification saved to Firestore") }
            .addOnFailureListener { e -> Log.e("FCM", "Failed to save notification", e) }
    }

    private fun updateDeviceToken(userId: String, token: String) {
        val tokenData = hashMapOf(
            "userId" to userId,
            "token" to token,
            "deviceInfo" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "lastUpdated" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("deviceTokens")
            .document(userId)
            .set(tokenData)
            .addOnSuccessListener { Log.d("FCM", "Device token updated") }
            .addOnFailureListener { e -> Log.e("FCM", "Failed to update device token", e) }
    }
}