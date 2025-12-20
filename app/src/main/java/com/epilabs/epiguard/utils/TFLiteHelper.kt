package com.epilabs.epiguard.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.epilabs.epiguard.data.repo.PredictionLogRepository
import com.epilabs.epiguard.models.PredictionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp

class TFLiteHelper(
    context: Context,
    private val predictionLogRepository: PredictionLogRepository = PredictionLogRepository()
) {
    companion object {
        private const val TAG = "TFLiteHelper"
    }

    private var interpreter: Interpreter
    private val frameBuffer = ArrayList<Bitmap>()
    private var firstFrameTime: Long? = null
    private val labels = arrayOf("Seizure", "Not Seizure")

    // Use coroutine scope for async Firebase operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track frame number within session
    private var frameNumber = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    init {
        val assetFileDescriptor = context.assets.openFd("model.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(modelBuffer, Interpreter.Options().apply {
            useXNNPACK = false
        })
        fileInputStream.close()
        Log.d(TAG, "TFLite interpreter initialized")
    }

    /**
     * Add frame and predict - Firebase version using your actual PredictionLogRepository
     * @param bitmap The frame to process
     * @param userId User ID (Firebase UID string)
     * @param sessionId Optional session ID for grouping predictions
     * @param videoId Optional video ID if recording
     * @return FloatArray of probabilities [seizure, notSeizure] or null if not ready
     */
    fun addFrameAndPredict(
        bitmap: Bitmap?,
        userId: String,
        sessionId: String = "",
        videoId: String = ""
    ): FloatArray? {
        if (bitmap == null || bitmap.isRecycled) {
            Log.e(TAG, "Null or recycled bitmap skipped")
            return null
        }

        val frameTime = System.currentTimeMillis()
        if (firstFrameTime == null) firstFrameTime = frameTime

        // Create a copy of the bitmap to avoid recycling issues
        val bitmapCopy = bitmap.config?.let { bitmap.copy(it, false) }
        if (bitmapCopy != null) {
            frameBuffer.add(bitmapCopy)
            Log.d(TAG, "Frame added. Buffer size: ${frameBuffer.size}, Bitmap: ${bitmapCopy.width}x${bitmapCopy.height}")
        }

        val elapsedTime = frameTime - (firstFrameTime ?: frameTime)
        if (frameBuffer.size < 10 && elapsedTime < 5000) {
            Log.d(TAG, "Waiting for 10 frames or 5 seconds, current size: ${frameBuffer.size}, elapsed: ${elapsedTime}ms")
            return null
        }

        Log.d(TAG, "Running inference with ${frameBuffer.size} frames")
        val inferenceStartTime = System.currentTimeMillis()
        val result = runInference(frameBuffer.toList())

        // Clear buffer and reset timer
        frameBuffer.forEach { if (!it.isRecycled) it.recycle() }
        frameBuffer.clear()
        firstFrameTime = null
        Log.d(TAG, "Inference completed in ${System.currentTimeMillis() - inferenceStartTime}ms")

        if (result == null) {
            Log.e(TAG, "Inference failed, no result")
            return null
        }

        Log.d(TAG, "Raw logits: ${result[0]}, ${result[1]}")
        val softmaxed = softmax(result)

        // Reverse to match your expected format [seizure, notSeizure]
        val reversedSoftmaxed = floatArrayOf(softmaxed[1], softmaxed[0])
        Log.d(TAG, "Softmaxed probs (reversed): Seizure ${reversedSoftmaxed[0]}, Not Seizure ${reversedSoftmaxed[1]}")

        val predictedIndex = reversedSoftmaxed.indices.maxByOrNull { reversedSoftmaxed[it] } ?: 0
        val label = labels[predictedIndex]
        val confidence = reversedSoftmaxed[predictedIndex]

        val timestamp = System.currentTimeMillis()
        frameNumber++

        // Save to Firebase using your PredictionLogRepository
        val predictionLog = PredictionLog(
            userId = userId,
            timestamp = timestamp,
            predictedLabel = label,
            confidence = confidence,
            rawScores = reversedSoftmaxed.toList(), // Convert FloatArray to List<Float> for Firestore
            frameNumber = frameNumber,
            videoId = videoId,
            sessionId = sessionId
        )

        // Save asynchronously to Firebase
        scope.launch {
            try {
                val result = predictionLogRepository.savePredictionLog(predictionLog)
                when (result) {
                    is com.epilabs.epiguard.utils.Result.Success -> {
                        Log.d(TAG, "Saved prediction to Firebase: ${result.data}")
                    }
                    is com.epilabs.epiguard.utils.Result.Error -> {
                        Log.e(TAG, "Failed to save prediction to Firebase: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving prediction to Firebase: ${e.message}", e)
            }
        }

        return reversedSoftmaxed
    }

    private fun runInference(frames: List<Bitmap>): FloatArray? {
        if (frames.isEmpty()) {
            Log.e(TAG, "No frames to process")
            return null
        }

        try {
            val inputBuffer = ByteBuffer.allocateDirect(1 * 10 * 224 * 224 * 3 * 4)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Process up to 10 frames
            val framesToProcess = frames.take(10)

            for (frame in framesToProcess) {
                if (frame.isRecycled) {
                    Log.e(TAG, "Skipping recycled bitmap in inference")
                    continue
                }

                try {
                    var tensorImage = TensorImage(DataType.FLOAT32)
                    tensorImage.load(frame)
                    tensorImage = imageProcessor.process(tensorImage)

                    val floatBuffer = tensorImage.buffer.asFloatBuffer()
                    val flatFrame = FloatArray(224 * 224 * 3)
                    floatBuffer.get(flatFrame)

                    for (value in flatFrame) {
                        inputBuffer.putFloat(value)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing frame: ${e.message}")
                    continue
                }
            }

            // Pad with zeros if we have fewer than 10 frames
            if (framesToProcess.size < 10) {
                val paddingSize = (10 - framesToProcess.size) * 224 * 224 * 3
                repeat(paddingSize) { inputBuffer.putFloat(0f) }
                Log.w(TAG, "Padded input with ${10 - framesToProcess.size} zeroed frames")
            }

            inputBuffer.rewind()

            val outputArray = Array(1) { FloatArray(2) }
            interpreter.run(inputBuffer, outputArray)

            return outputArray[0]
        } catch (e: Exception) {
            Log.e(TAG, "Inference error: ${e.message}", e)
            return null
        }
    }

    private fun softmax(logits: FloatArray): FloatArray {
        if (logits.isEmpty()) return floatArrayOf()

        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp((it - maxLogit).toDouble()) }
        val sumExp = exps.sum()

        return if (sumExp > 0) {
            exps.map { (it / sumExp).toFloat() }.toFloatArray()
        } else {
            FloatArray(logits.size) { 1f / logits.size }
        }
    }

    fun close() {
        try {
            interpreter.close()
            frameBuffer.forEach { if (!it.isRecycled) it.recycle() }
            frameBuffer.clear()
            frameNumber = 0
            Log.d(TAG, "Interpreter closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing TFLiteHelper: ${e.message}", e)
        }
    }
}