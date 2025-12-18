package com.epilabs.epiguard.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager

/**
 * Utility class to manage wake locks and prevent screen timeout during seizure detection
 * Critical for medical monitoring applications that need continuous operation
 */
class WakeLockManager(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isWakeLockHeld = false

    companion object {
        private const val TAG = "WakeLockManager"
        private const val WAKE_LOCK_TAG = "EpiGuard::SeizureDetection"
    }

    /**
     * Acquire wake lock to prevent device from sleeping during detection
     * Call this when starting seizure detection
     */
    @SuppressLint("WakelockTimeout")
    fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                Log.d(TAG, "Wake lock already held")
                return
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                WAKE_LOCK_TAG
            )

            // Acquire wake lock indefinitely (until explicitly released)
            wakeLock?.acquire()
            isWakeLockHeld = true

            Log.d(TAG, "Wake lock acquired for seizure detection")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    /**
     * Release wake lock when detection stops
     * Call this when stopping seizure detection
     */
    fun releaseWakeLock() {
        try {
            wakeLock?.let { wl ->
                if (wl.isHeld) {
                    wl.release()
                    isWakeLockHeld = false
                    Log.d(TAG, "Wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock", e)
        }
    }

    /**
     * Keep screen on during detection (call from Activity)
     * This prevents screen dimming/turning off
     */
    fun keepScreenOn(activity: Activity, keepOn: Boolean) {
        try {
            if (keepOn) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.d(TAG, "Screen will stay on during detection")
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.d(TAG, "Screen timeout restored")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set screen flags", e)
        }
    }

    /**
     * Check if wake lock is currently held
     */
    fun isWakeLockHeld(): Boolean = isWakeLockHeld

    /**
     * Cleanup - call in onDestroy or DisposableEffect
     */
    fun cleanup() {
        releaseWakeLock()
    }
}