package com.epilabs.epiguard.data.repo

import com.epilabs.epiguard.models.AppNotification
import com.epilabs.epiguard.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getNotifications(): Result<List<AppNotification>> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val querySnapshot = firestore.collection("notifications")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val notifications = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(AppNotification::class.java)?.copy(notificationId = doc.id)
            }

            Result.Success(notifications)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get notifications")
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to mark notification as read")
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete notification")
        }
    }

    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val querySnapshot = firestore.collection("notifications")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            Result.Success(querySnapshot.size())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get unread count")
        }
    }

    /**
     * Send detection started notification to user and primary contact
     */
    suspend fun sendDetectionStartedNotification(streamUrl: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")
            val timestamp = System.currentTimeMillis()
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

            // Get user details and primary contact
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val primaryContactId = userDoc.getString("primaryContactId")
            val userName = userDoc.getString("name") ?: "User"

            val notifications = mutableListOf<Map<String, Any>>()

            // Notification for the user
            notifications.add(
                hashMapOf(
                    "userId" to currentUser.uid,
                    "title" to "Detection Started",
                    "message" to "Seizure detection monitoring has been activated at ${formatter.format(timestamp)}",
                    "type" to "DETECTION_START",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "metadata" to mapOf(
                        "streamUrl" to streamUrl,
                        "sessionId" to timestamp.toString()
                    )
                )
            )

            // Notification for primary contact if exists
            if (!primaryContactId.isNullOrEmpty()) {
                notifications.add(
                    hashMapOf(
                        "userId" to primaryContactId,
                        "title" to "$userName - Detection Started",
                        "message" to "$userName has started seizure detection monitoring at ${formatter.format(timestamp)}",
                        "type" to "DETECTION_START",
                        "timestamp" to timestamp,
                        "isRead" to false,
                        "metadata" to mapOf(
                            "monitoredUserId" to currentUser.uid,
                            "monitoredUserName" to userName,
                            "sessionId" to timestamp.toString()
                        )
                    )
                )
            }

            // Save to Firestore
            val batch = firestore.batch()
            notifications.forEach { notification ->
                val docRef = firestore.collection("notifications").document()
                batch.set(docRef, notification)
            }
            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send detection started notification")
        }
    }

    /**
     * Send detection stopped notification to user and primary contact
     */
    suspend fun sendDetectionStoppedNotification(durationMs: Long, sessionId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")
            val timestamp = System.currentTimeMillis()
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val durationMinutes = durationMs / 1000 / 60

            // Get user details and primary contact
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val primaryContactId = userDoc.getString("primaryContactId")
            val userName = userDoc.getString("name") ?: "User"

            val notifications = mutableListOf<Map<String, Any>>()

            // Notification for the user
            notifications.add(
                hashMapOf(
                    "userId" to currentUser.uid,
                    "title" to "Detection Stopped",
                    "message" to "Seizure detection monitoring ended after $durationMinutes minutes at ${formatter.format(timestamp)}",
                    "type" to "DETECTION_STOP",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "metadata" to mapOf(
                        "durationMs" to durationMs,
                        "durationMinutes" to durationMinutes,
                        "sessionId" to sessionId
                    )
                )
            )

            // Notification for primary contact if exists
            if (!primaryContactId.isNullOrEmpty()) {
                notifications.add(
                    hashMapOf(
                        "userId" to primaryContactId,
                        "title" to "$userName - Detection Stopped",
                        "message" to "$userName has stopped seizure detection monitoring after $durationMinutes minutes",
                        "type" to "DETECTION_STOP",
                        "timestamp" to timestamp,
                        "isRead" to false,
                        "metadata" to mapOf(
                            "monitoredUserId" to currentUser.uid,
                            "monitoredUserName" to userName,
                            "durationMs" to durationMs,
                            "durationMinutes" to durationMinutes,
                            "sessionId" to sessionId
                        )
                    )
                )
            }

            // Save to Firestore
            val batch = firestore.batch()
            notifications.forEach { notification ->
                val docRef = firestore.collection("notifications").document()
                batch.set(docRef, notification)
            }
            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send detection stopped notification")
        }
    }

    /**
     * Send seizure detected notification to user and primary contact
     */
    suspend fun sendSeizureDetectedNotification(timestampRange: String, message: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")
            val timestamp = System.currentTimeMillis()

            // Get user details and primary contact
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val primaryContactId = userDoc.getString("primaryContactId")
            val userName = userDoc.getString("name") ?: "User"

            val notifications = mutableListOf<Map<String, Any>>()

            // Notification for user
            notifications.add(
                hashMapOf(
                    "userId" to currentUser.uid,
                    "title" to "⚠️ SEIZURE DETECTED",
                    "message" to message,
                    "type" to "SEIZURE_ALERT",
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "priority" to "HIGH",
                    "metadata" to mapOf(
                        "detectionTime" to timestampRange
                    )
                )
            )

            // Notification for primary contact
            if (!primaryContactId.isNullOrEmpty()) {
                notifications.add(
                    hashMapOf(
                        "userId" to primaryContactId,
                        "title" to "🚨 $userName - SEIZURE ALERT",
                        "message" to "$userName may be having a seizure. Detection time: $timestampRange",
                        "type" to "SEIZURE_ALERT",
                        "timestamp" to timestamp,
                        "isRead" to false,
                        "priority" to "CRITICAL",
                        "metadata" to mapOf(
                            "monitoredUserId" to currentUser.uid,
                            "monitoredUserName" to userName,
                            "detectionTime" to timestampRange
                        )
                    )
                )
            }

            // Save to Firestore
            val batch = firestore.batch()
            notifications.forEach { notification ->
                val docRef = firestore.collection("notifications").document()
                batch.set(docRef, notification)
            }
            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send seizure detected notification")
        }
    }
}