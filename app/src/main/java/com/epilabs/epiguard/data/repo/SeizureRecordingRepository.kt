package com.epilabs.epiguard.seizure

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.epilabs.epiguard.data.repo.NotificationRepository
import com.epilabs.epiguard.data.repo.PredictionLogRepository
import com.epilabs.epiguard.network.TwilioApi
import com.epilabs.epiguard.utils.TFLiteHelper
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.coroutineContext

/**
 * Pure seizure detection class - Firebase version
 * Handles stream processing and AI inference with Firebase storage
 */
class IpMjpegDetector(
    private val context: Context,
    private val userId: String, // Firebase UID (String)
    private val streamUrl: String,
    private val seizureConfidenceThreshold: Float = 0.80f,
    private val consecutiveLimit: Int = 3,
    private val inferenceIntervalMs: Long = 5_000L,
    private val maxConnectRetries: Int = 3,
    private val twilioToNumber: String,
    private val twilioFromNumber: String,
    private val twilioApi: TwilioApi = TwilioApi(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
    private val predictionLogRepository: PredictionLogRepository = PredictionLogRepository(),
    // Callbacks for UI
    private val onFrame: (Bitmap) -> Unit = {},
    private val onPrediction: (timestampRange: String, label: String, confidence: Float) -> Unit = { _, _, _ -> },
    private val onSeizureDetected: (String, String) -> Unit = { _, _ -> },
    private val onStreamStatus: (String) -> Unit = {},
    private val onDetectionStarted: () -> Unit = {},
    private val onDetectionStopped: (Long) -> Unit = {}
) {
    companion object {
        private const val TAG = "IpMjpegDetector"
    }

    private val tflite = TFLiteHelper(
        context = context,
        predictionLogRepository = predictionLogRepository
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var streamJob: Job? = null
    private var inferenceJob: Job? = null

    private val latestFrameRef = AtomicReference<Bitmap?>(null)

    private var streaming = false
    private var consecutiveSeizures = 0
    private var detectionStartTime: Long = 0
    private var detectionSessionId: String = ""

    fun start() {
        if (streaming) return
        streaming = true
        detectionStartTime = System.currentTimeMillis()
        detectionSessionId = "session_${detectionStartTime}_${userId}"
        onStreamStatus("Connecting")
        consecutiveSeizures = 0

        // Notify that detection started
        onDetectionStarted()

        // Send Firebase notification for detection start
        scope.launch {
            try {
                notificationRepository.sendDetectionStartedNotification(streamUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send detection started notification: ${e.message}")
            }
        }

        streamJob = scope.launch(Dispatchers.IO) { streamLoop() }
        inferenceJob = scope.launch(Dispatchers.Default) { inferenceLoop() }
    }

    fun stop() {
        if (!streaming) return

        streaming = false
        val detectionDuration = System.currentTimeMillis() - detectionStartTime

        streamJob?.cancel(); streamJob = null
        inferenceJob?.cancel(); inferenceJob = null
        latestFrameRef.getAndSet(null)?.recycle()
        tflite.close()
        onStreamStatus("Stopped")

        // Send Firebase notification for detection stop
        scope.launch {
            try {
                notificationRepository.sendDetectionStoppedNotification(
                    detectionDuration,
                    detectionSessionId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send detection stopped notification: ${e.message}")
            }
        }

        // Notify that detection stopped with duration
        onDetectionStopped(detectionDuration)
    }

    fun isRunning(): Boolean = streaming

    private suspend fun streamLoop() {
        var retries = 0
        while (coroutineContext.isActive && streaming && retries < maxConnectRetries) {
            var conn: HttpURLConnection? = null
            var ins: InputStream? = null
            try {
                onStreamStatus("Connecting (${retries + 1}/$maxConnectRetries)")
                val url = URL(streamUrl)
                conn = (url.openConnection() as HttpURLConnection).apply {
                    readTimeout = 15_000
                    connectTimeout = 10_000
                    requestMethod = "GET"
                    doInput = true
                    connect()
                }
                ins = conn.inputStream
                val reader = MjpegStreamReader(ins)
                onStreamStatus("Streaming")

                while (coroutineContext.isActive && streaming) {
                    val bmp = reader.readFrameBitmap() ?: continue

                    val uiCopy = bmp.copy(Bitmap.Config.ARGB_8888, false)
                    val infCopy = bmp.copy(Bitmap.Config.ARGB_8888, false)
                    bmp.recycle()

                    withContext(Dispatchers.Main) { onFrame(uiCopy) }
                    latestFrameRef.getAndSet(infCopy)?.let { old ->
                        if (!old.isRecycled) old.recycle()
                    }
                    retries = 0
                }
            } catch (e: Exception) {
                Log.e(TAG, "Stream error: ${e.message}", e)
                onStreamStatus("Error: ${e.message}")
                retries++
                if (retries >= maxConnectRetries) {
                    onStreamStatus("Disconnected")
                    streaming = false
                    break
                }
                delay(1_000)
            } finally {
                try { ins?.close() } catch (_: Exception) {}
                try { conn?.disconnect() } catch (_: Exception) {}
            }
        }
    }

    private suspend fun inferenceLoop() {
        val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        var nextTick = System.currentTimeMillis()

        while (coroutineContext.isActive && streaming) {
            val windowEnd = System.currentTimeMillis()
            val windowStart = windowEnd - inferenceIntervalMs
            val tsRange = "${fmt.format(windowStart)} - ${fmt.format(windowEnd)}"

            try {
                val frame = latestFrameRef.getAndSet(null)
                if (frame != null && !frame.isRecycled) {
                    val probs = try {
                        tflite.addFrameAndPredict(
                            bitmap = frame,
                            userId = userId,
                            sessionId = detectionSessionId
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Inference error: ${e.message}", e)
                        null
                    } finally {
                        if (!frame.isRecycled) frame.recycle()
                    }

                    if (probs != null && probs.size >= 2) {
                        val seizureP = probs[0]
                        val notSeizureP = probs[1]
                        val label = if (seizureP >= notSeizureP) "Seizure" else "Not Seizure"
                        val conf = maxOf(seizureP, notSeizureP)

                        withContext(Dispatchers.Main) { onPrediction(tsRange, label, conf) }

                        if (label == "Seizure" && conf >= seizureConfidenceThreshold) {
                            consecutiveSeizures++
                            if (consecutiveSeizures >= consecutiveLimit) {
                                consecutiveSeizures = 0
                                triggerSeizureAlert(tsRange)
                            }
                        } else {
                            consecutiveSeizures = 0
                        }
                    } else {
                        Log.d(TAG, "No valid probabilities.")
                    }
                } else {
                    Log.d(TAG, "No frame for inference.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Inference loop error: ${e.message}", e)
            }

            nextTick += inferenceIntervalMs
            val delayMs = (nextTick - System.currentTimeMillis()).coerceAtLeast(0)
            delay(delayMs)
        }
    }

    private suspend fun triggerSeizureAlert(tsRange: String) {
        val msg = "Seizure detected ($tsRange). Please check immediately."

        // Save notification to Firebase via NotificationRepository
        try {
            notificationRepository.sendSeizureDetectedNotification(tsRange, msg)
            Log.d(TAG, "Seizure notification sent to Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send Firebase notification: ${e.message}", e)
        }

        // Send SMS via Twilio (only for seizure alerts, not start/stop)
        val twilioSuccess = withContext(Dispatchers.IO) {
            try {
                twilioApi.sendSmsBlocking(
                    to = twilioToNumber,
                    from = twilioFromNumber,
                    body = msg
                )
            } catch (e: Exception) {
                Log.e(TAG, "Twilio SMS failed: ${e.message}", e)
                false
            }
        }

        // Notify via callback (screen will handle UI updates)
        onSeizureDetected(tsRange, msg)

        Log.d(TAG, "Seizure alert triggered - Firebase: success, Twilio: $twilioSuccess")
    }
}

/* MJPEG Reader - unchanged */
private class MjpegStreamReader(private val input: InputStream) {
    private val startMarker = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val endMarker   = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    fun readFrameBitmap(): Bitmap? {
        var buffer: ByteArrayOutputStream? = null
        var retries = 0
        val maxRetries = 3

        while (retries < maxRetries) {
            try {
                buffer = ByteArrayOutputStream()
                val readBuf = ByteArray(4096)
                var foundStart = false

                while (true) {
                    val r = input.read(readBuf)
                    if (r <= 0) {
                        buffer.close()
                        return null
                    }
                    val chunk = readBuf.copyOf(r)
                    if (!foundStart) {
                        val si = indexOf(chunk, startMarker)
                        if (si >= 0) {
                            buffer.write(chunk, si, chunk.size - si)
                            foundStart = true
                            val ei = indexOf(chunk, endMarker, si)
                            if (ei >= 0) {
                                val len = ei + endMarker.size - si
                                val frameBytes = chunk.copyOfRange(si, si + len)
                                buffer.close()
                                return BitmapFactory.decodeByteArray(frameBytes, 0, frameBytes.size)
                            }
                        }
                    } else {
                        val ei = indexOf(chunk, endMarker)
                        if (ei >= 0) {
                            buffer.write(chunk, 0, ei + endMarker.size)
                            val frameBytes = buffer.toByteArray()
                            buffer.close()
                            return BitmapFactory.decodeByteArray(frameBytes, 0, frameBytes.size)
                        } else {
                            buffer.write(chunk)
                        }
                    }
                }
            } catch (e: Exception) {
                retries++
                try { buffer?.close() } catch (_: Exception) {}
                if (retries < maxRetries) {
                    Thread.sleep(100)
                    continue
                }
                Log.e("MjpegStreamReader", "Failed frame read: ${e.message}", e)
                return null
            }
        }
        return null
    }

    private fun indexOf(data: ByteArray, pattern: ByteArray, startOffset: Int = 0): Int {
        outer@ for (i in startOffset..data.size - pattern.size) {
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) continue@outer
            }
            return i
        }
        return -1
    }
}