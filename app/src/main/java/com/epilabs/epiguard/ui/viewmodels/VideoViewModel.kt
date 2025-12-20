// VideoViewModel.kt - Additional methods that need to be added
package com.epilabs.epiguard.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.epilabs.epiguard.data.repo.VideoRepository
import com.epilabs.epiguard.models.VideoModel
import com.epilabs.epiguard.models.TestResult
import com.epilabs.epiguard.models.VideoPredictionResult
import com.epilabs.epiguard.utils.Result

class VideoViewModel(
    private val context: Context,
    private val userId: String
) : ViewModel() {

    private val videoRepository = VideoRepository()

    private val _videos = MutableStateFlow<List<VideoModel>>(emptyList())
    val videos: StateFlow<List<VideoModel>> = _videos.asStateFlow()

    private val _testResults = MutableStateFlow<List<TestResult>>(emptyList())
    val testResults: StateFlow<List<TestResult>> = _testResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isClassifying = MutableStateFlow(false)
    val isClassifying: StateFlow<Boolean> = _isClassifying.asStateFlow()

    private val _classificationProgress = MutableStateFlow(0)
    val classificationProgress: StateFlow<Int> = _classificationProgress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadVideos()
        loadTestResults()
    }

    // FIXED: Add explicit load methods
    fun loadVideos() {
        viewModelScope.launch {
            videoRepository.getVideosForUser(userId).collect { videoList ->
                _videos.value = videoList
            }
        }
    }

    fun loadTestResults() {
        viewModelScope.launch {
            when (val result = videoRepository.getTestResultsForUser(userId)) {
                is Result.Success -> {
                    _testResults.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun uploadVideo(uri: Uri, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = videoRepository.uploadVideo(userId, uri, context)) {
                is Result.Success -> {
                    callback(true, null)
                }
                is Result.Error -> {
                    callback(false, result.message)
                }
            }

            _isLoading.value = false
        }
    }

    fun classifyVideo(
        video: VideoModel,
        modelName: String,
        callback: (Boolean, TestResult?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isClassifying.value = true
            _classificationProgress.value = 0

            try {
                // Simulate classification progress
                for (progress in 0..100 step 10) {
                    _classificationProgress.value = progress
                    kotlinx.coroutines.delay(200) // Simulate processing time
                }

                // Create mock predictions for demonstration
                val mockPredictions = listOf(
                    VideoPredictionResult(
                        timestamp = 1000L,
                        predictedLabel = "normal",
                        confidence = 0.85f
                    ),
                    VideoPredictionResult(
                        timestamp = 5000L,
                        predictedLabel = "seizure_activity",
                        confidence = 0.92f
                    ),
                    VideoPredictionResult(
                        timestamp = 8000L,
                        predictedLabel = "normal",
                        confidence = 0.78f
                    )
                )

                // Save test result
                when (val result = videoRepository.saveTestResult(
                    userId = userId,
                    videoId = video.id,
                    videoName = video.name,
                    modelName = modelName,
                    predictions = mockPredictions
                )) {
                    is Result.Success -> {
                        // Update video status to analyzed
                        videoRepository.updateVideoStatus(video.id, VideoModel.VideoStatus.ANALYZED)
                        callback(true, result.data, null)

                        // Refresh test results
                        loadTestResults()
                    }
                    is Result.Error -> {
                        callback(false, null, result.message)
                    }
                }
            } catch (e: Exception) {
                callback(false, null, "Classification failed: ${e.message}")
            } finally {
                _isClassifying.value = false
                _classificationProgress.value = 0
            }
        }
    }

    fun deleteVideo(videoId: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = videoRepository.deleteVideo(videoId, context)) {
                is Result.Success -> {
                    callback(true, null)
                }
                is Result.Error -> {
                    callback(false, result.message)
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(
        private val context: Context,
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VideoViewModel::class.java)) {
                return VideoViewModel(context, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}