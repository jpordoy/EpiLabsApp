package com.epilabs.epiguard.data.repo

import com.epilabs.epiguard.models.SeizureRecording
import com.epilabs.epiguard.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class SeizureRecordingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Save a seizure recording to Firestore
     */
    suspend fun saveSeizureRecording(recording: SeizureRecording): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val recordingWithUserId = recording.copy(userId = currentUser.uid)
            val docRef = firestore.collection("seizureRecordings")
                .add(recordingWithUserId)
                .await()

            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save seizure recording")
        }
    }

    /**
     * Upload video file to Firebase Storage
     */
    suspend fun uploadVideoFile(recordingId: String, videoFile: File): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val storageRef = storage.reference
                .child("seizure_recordings")
                .child(currentUser.uid)
                .child("$recordingId.mp4")

            val uploadTask = storageRef.putFile(android.net.Uri.fromFile(videoFile)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            // Update Firestore with video URL
            firestore.collection("seizureRecordings")
                .document(recordingId)
                .update(mapOf(
                    "videoUrl" to downloadUrl,
                    "isProcessing" to false
                ))
                .await()

            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to upload video")
        }
    }

    /**
     * Get all seizure recordings for current user
     */
    suspend fun getSeizureRecordings(): Result<List<SeizureRecording>> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val querySnapshot = firestore.collection("seizureRecordings")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("detectionTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val recordings = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(SeizureRecording::class.java)?.copy(id = doc.id)
            }

            Result.Success(recordings)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get seizure recordings")
        }
    }

    /**
     * Get a specific seizure recording
     */
    suspend fun getSeizureRecording(recordingId: String): Result<SeizureRecording?> {
        return try {
            val doc = firestore.collection("seizureRecordings")
                .document(recordingId)
                .get()
                .await()

            val recording = doc.toObject(SeizureRecording::class.java)?.copy(id = doc.id)
            Result.Success(recording)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get seizure recording")
        }
    }

    /**
     * Delete a seizure recording
     */
    suspend fun deleteSeizureRecording(recordingId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            // Get recording to find video URL
            val doc = firestore.collection("seizureRecordings")
                .document(recordingId)
                .get()
                .await()

            val recording = doc.toObject(SeizureRecording::class.java)

            // Delete video from Storage if exists
            recording?.videoUrl?.let { url ->
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    // Log but don't fail if video deletion fails
                    android.util.Log.e("SeizureRecordingRepo", "Failed to delete video", e)
                }
            }

            // Delete Firestore document
            firestore.collection("seizureRecordings")
                .document(recordingId)
                .delete()
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete seizure recording")
        }
    }

    /**
     * Update seizure recording notes
     */
    suspend fun updateNotes(recordingId: String, notes: String): Result<Unit> {
        return try {
            firestore.collection("seizureRecordings")
                .document(recordingId)
                .update("notes", notes)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update notes")
        }
    }
}