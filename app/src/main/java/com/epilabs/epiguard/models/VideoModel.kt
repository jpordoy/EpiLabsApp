// VideoModel.kt
package com.epilabs.epiguard.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class VideoModel(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val localPath: String = "",
    val firebaseUrl: String? = null,
    @ServerTimestamp
    val uploadDate: Date? = null,
    val status: VideoStatus = VideoStatus.UPLOADED,
    val fileSize: Long = 0L,
    val duration: Long = 0L, // in milliseconds
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    enum class VideoStatus {
        UPLOADING,
        UPLOADED,
        PROCESSING,
        ANALYZED,
        ERROR,
        RESULT
    }
}
