package com.epilabs.epiguard.data.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.epilabs.epiguard.models.TestResult
import com.epilabs.epiguard.models.VideoModel
import com.epilabs.epiguard.models.VideoPredictionResult
import com.epilabs.epiguard.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID

class VideoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val videosCollection = firestore.collection("videos")
    private val testResultsCollection = firestore.collection("test_results")

    companion object {
        private const val TAG = "VideoRepository"
    }

    // Get all videos for a user
    fun getVideosForUser(userId: String): Flow<List<VideoModel>> = callbackFlow {
        val listener = videosCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to videos", error)
                    close(error)
                    return@addSnapshotListener
                }

                val videos = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(VideoModel::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to VideoModel", e)
                        null
                    }
                } ?: emptyList()

                trySend(videos)
            }

        awaitClose { listener.remove() }
    }

    // Upload video to local storage and save metadata to Firestore
    suspend fun uploadVideo(
        userId: String,
        videoUri: Uri,
        context: Context
    ): Result<VideoModel> {
        return try {
            // Generate unique filename
            val fileName = "video_${System.currentTimeMillis()}.mp4"

            // Save to local storage
            val videoDir = File(context.filesDir, "videos").apply { mkdirs() }
            val localFile = File(videoDir, fileName)

            context.contentResolver.openInputStream(videoUri)?.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Get file size
            val fileSize = localFile.length()

            // Create video model
            val videoModel = VideoModel(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = fileName,
                localPath = localFile.absolutePath,
                status = VideoModel.VideoStatus.UPLOADED,
                fileSize = fileSize,
                createdAt = Date(),
                updatedAt = Date()
            )

            // Save to Firestore
            videosCollection.document(videoModel.id).set(videoModel).await()

            Log.d(TAG, "Video uploaded successfully: ${videoModel.id}")
            Result.Success(videoModel)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading video", e)
            Result.Error("Failed to upload video: ${e.message}")
        }
    }

    // Get specific video by ID
    suspend fun getVideoById(videoId: String): Result<VideoModel?> {
        return try {
            val document = videosCollection.document(videoId).get().await()
            val video = if (document.exists()) {
                document.toObject(VideoModel::class.java)?.copy(id = document.id)
            } else {
                null
            }
            Result.Success(video)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video by ID", e)
            Result.Error("Failed to get video: ${e.message}")
        }
    }

    // Delete video
    suspend fun deleteVideo(videoId: String, context: Context): Result<Unit> {
        return try {
            // Get video first to delete local file
            val videoResult = getVideoById(videoId)
            if (videoResult is Result.Success && videoResult.data != null) {
                val video = videoResult.data
                // Delete local file
                val localFile = File(video.localPath)
                if (localFile.exists()) {
                    localFile.delete()
                }
            }

            // Delete from Firestore
            videosCollection.document(videoId).delete().await()

            // Delete associated test results
            val testResults = testResultsCollection
                .whereEqualTo("videoId", videoId)
                .get()
                .await()

            val batch = firestore.batch()
            testResults.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Log.d(TAG, "Video deleted successfully: $videoId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting video", e)
            Result.Error("Failed to delete video: ${e.message}")
        }
    }

    // Save test results
    suspend fun saveTestResult(
        userId: String,
        videoId: String,
        videoName: String,
        modelName: String,
        predictions: List<VideoPredictionResult>
    ): Result<TestResult> {
        return try {
            val overallConfidence = if (predictions.isNotEmpty()) {
                predictions.map { it.confidence }.average().toFloat()
            } else {
                0f
            }

            val seizureDetected = predictions.any {
                it.predictedLabel.contains("seizure", ignoreCase = true) && it.confidence > 0.5f
            }

            val testResult = TestResult(
                id = UUID.randomUUID().toString(),
                userId = userId,
                videoId = videoId,
                videoName = videoName,
                modelName = modelName,
                predictions = predictions,
                overallConfidence = overallConfidence,
                seizureDetected = seizureDetected,
                analysisDate = Date()
            )

            testResultsCollection.document(testResult.id).set(testResult).await()

            Log.d(TAG, "Test result saved successfully: ${testResult.id}")
            Result.Success(testResult)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving test result", e)
            Result.Error("Failed to save test result: ${e.message}")
        }
    }

    // Get test results for a video
    suspend fun getTestResultsForVideo(videoId: String): Result<List<TestResult>> {
        return try {
            val snapshot = testResultsCollection
                .whereEqualTo("videoId", videoId)
                .orderBy("analysisDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val results = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(TestResult::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to TestResult", e)
                    null
                }
            }

            Result.Success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting test results", e)
            Result.Error("Failed to get test results: ${e.message}")
        }
    }

    // UPDATED: Get all test results for a user with fallback for missing index
    suspend fun getTestResultsForUser(userId: String): Result<List<TestResult>> {
        return try {
            // First try the optimized query with ordering
            val snapshot = testResultsCollection
                .whereEqualTo("userId", userId)
                .orderBy("analysisDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val results = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(TestResult::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to TestResult", e)
                    null
                }
            }

            Result.Success(results)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                // Fallback to simple query without ordering if index is missing
                return try {
                    Log.w(TAG, "Using fallback query for test_results. Please create Firestore index for better performance.")

                    val fallbackSnapshot = testResultsCollection
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()

                    val results = fallbackSnapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(TestResult::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting document to TestResult", e)
                            null
                        }
                    }.sortedByDescending { it.analysisDate } // Sort in memory

                    Result.Success(results)
                } catch (fallbackError: Exception) {
                    Log.e(TAG, "Error with fallback query", fallbackError)
                    Result.Error("Failed to get test results: ${fallbackError.message}")
                }
            } else {
                Log.e(TAG, "Error getting test results for user", e)
                Result.Error("Failed to get test results: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting test results", e)
            Result.Error("Failed to get test results: ${e.message}")
        }
    }

    // Update video status
    suspend fun updateVideoStatus(videoId: String, status: VideoModel.VideoStatus): Result<Unit> {
        return try {
            videosCollection.document(videoId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Date()
                    )
                ).await()

            Log.d(TAG, "Video status updated: $videoId -> $status")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating video status", e)
            Result.Error("Failed to update video status: ${e.message}")
        }
    }
}