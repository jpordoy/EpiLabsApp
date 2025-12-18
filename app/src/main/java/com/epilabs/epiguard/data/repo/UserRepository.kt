package com.epilabs.epiguard.data.repo

import com.epilabs.epiguard.models.User
import com.epilabs.epiguard.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.InputStream

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.Success(null)
            } else {
                val document = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val user = document.toObject(User::class.java)
                Result.Success(user)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get user")
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update user")
        }
    }

    suspend fun uploadProfileImage(userId: String, imageStream: InputStream): Result<String> {
        return try {
            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            val uploadTask = imageRef.putStream(imageStream).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to upload image")
        }
    }

    suspend fun findUserByEmail(email: String): Result<User?> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            val user = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].toObject(User::class.java)
            } else null

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to find user")
        }
    }
}