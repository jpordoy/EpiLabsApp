package com.epilabs.epiguard.data.repo

import com.epilabs.epiguard.models.PredictionLog
import com.epilabs.epiguard.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PredictionLogRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun savePredictionLog(predictionLog: PredictionLog): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val logWithUserId = predictionLog.copy(userId = currentUser.uid)
            val docRef = firestore.collection("predictionLogs")
                .add(logWithUserId)
                .await()

            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save prediction log")
        }
    }

    suspend fun getPredictionLogs(sessionId: String? = null): Result<List<PredictionLog>> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            var query = firestore.collection("predictionLogs")
                .whereEqualTo("userId", currentUser.uid)

            if (sessionId != null) {
                query = query.whereEqualTo("sessionId", sessionId)
            }

            val querySnapshot = query
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val logs = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(PredictionLog::class.java)?.copy(id = doc.id)
            }

            Result.Success(logs)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get prediction logs")
        }
    }
}