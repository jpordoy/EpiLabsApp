package com.epilabs.epiguard.data.repo

import android.content.Context
import android.util.Log
import com.epilabs.epiguard.R
import com.epilabs.epiguard.models.User
import com.epilabs.epiguard.utils.Result
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun getGoogleSignInClient(): GoogleSignInClient {
        Log.d(TAG, "Creating GoogleSignInClient")

        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get web client ID", e)
            throw e
        }

        Log.d(TAG, "Web Client ID: ${webClientId.take(20)}...") // Log first 20 chars for security

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso).also {
            Log.d(TAG, "GoogleSignInClient created successfully")
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "Starting Google sign-in with idToken")
            Log.d(TAG, "Firebase Auth URL: ${auth.app.options.apiKey}")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Log.d(TAG, "Google credential created")

            val authResult = auth.signInWithCredential(credential).await()
            Log.d(TAG, "Firebase authentication successful")
            val firebaseUser = authResult.user!!
            Log.d(TAG, "Firebase user obtained: ${firebaseUser.uid}")

            // Check if this is a new user
            val isNewUser = authResult.additionalUserInfo?.isNewUser == true
            Log.d(TAG, "Is new user: $isNewUser")

            if (isNewUser) {
                Log.d(TAG, "Creating new user document")

                // Create user document for new Google users
                val user = User(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "User",
                    firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "",
                    lastName = firebaseUser.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                    email = firebaseUser.email ?: "",
                    contactNumber = "", // Will be filled later by user
                    deviceTokens = emptyList()
                )

                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()

                Log.d(TAG, "User document created successfully")
            }

            // Update FCM token
            Log.d(TAG, "Getting FCM token")
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM token obtained")

            updateDeviceToken(firebaseUser.uid, fcmToken)
            Log.d(TAG, "Device token updated")

            Log.d(TAG, "Google sign-in completed successfully")
            Result.Success(firebaseUser)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()
            Result.Error(e.message ?: "Google sign in failed")
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        contactNumber: String
    ): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "Starting email sign-up")

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!

            // Get FCM token
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            // Create user document in Firestore
            val user = User(
                userId = firebaseUser.uid,
                username = username,
                firstName = firstName,
                lastName = lastName,
                email = email,
                contactNumber = contactNumber,
                deviceTokens = listOf(fcmToken)
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            // Update device token
            updateDeviceToken(firebaseUser.uid, fcmToken)

            Log.d(TAG, "Sign-up completed successfully")
            Result.Success(firebaseUser)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-up failed", e)
            Result.Error(e.message ?: "Sign up failed")
        }
    }

    // New extended signup method with additional fields
    suspend fun signUpExtended(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        contactNumber: String,
        gender: String,
        dateOfBirth: Long,
        profileImageUrl: String = ""
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!

            // Get FCM token
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            // Create user document in Firestore with all fields
            val user = User(
                userId = firebaseUser.uid,
                username = username,
                firstName = firstName,
                lastName = lastName,
                email = email,
                contactNumber = contactNumber,
                profileImageUrl = profileImageUrl,
                gender = gender,
                dateOfBirth = dateOfBirth,
                deviceTokens = listOf(fcmToken)
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            // Update device token
            updateDeviceToken(firebaseUser.uid, fcmToken)

            Result.Success(firebaseUser)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "Starting email sign-in")

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!

            // Update FCM token on sign in
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            updateDeviceToken(firebaseUser.uid, fcmToken)

            Log.d(TAG, "Sign-in completed successfully")
            Result.Success(firebaseUser)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed", e)
            Result.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send reset email")
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send verification email")
        }
    }

    fun signOut() {
        Log.d(TAG, "Signing out")
        auth.signOut()
        getGoogleSignInClient().signOut()
        Log.d(TAG, "Sign-out completed")
    }

    private suspend fun updateDeviceToken(userId: String, token: String) {
        try {
            val tokenData = hashMapOf(
                "userId" to userId,
                "token" to token,
                "deviceInfo" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection("deviceTokens")
                .document(userId)
                .set(tokenData)
                .await()
        } catch (e: Exception) {
            // Log but don't fail the auth operation
            Log.e(TAG, "Failed to update device token", e)
        }
    }
}