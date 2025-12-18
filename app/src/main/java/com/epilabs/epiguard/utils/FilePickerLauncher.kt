package com.epilabs.epiguard.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

class FilePickerLauncher(
    private val onUriSelected: (Uri) -> Unit,
    private val onError: (String) -> Unit,
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    fun launchPicker() {
        try {
            launcher.launch("video/*")
        } catch (e: Exception) {
            onError("Failed to open file picker: ${e.message}")
        }
    }

    companion object {
        @Composable
        fun create(
            onUriSelected: (Uri) -> Unit,
            onError: (String) -> Unit
        ): FilePickerLauncher {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    onUriSelected(uri)
                } else {
                    onError("No video file selected")
                }
            }

            return FilePickerLauncher(onUriSelected, onError, launcher)
        }
    }
}