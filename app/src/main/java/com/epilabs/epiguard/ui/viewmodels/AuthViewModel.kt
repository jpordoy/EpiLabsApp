package com.epilabs.epiguard.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.epilabs.epiguard.data.repo.AuthRepository
import com.epilabs.epiguard.utils.Result
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application.applicationContext)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authRepository.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Success(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (account == null) {
                _authState.value = AuthState.Error("Google sign in was cancelled")
                return@launch
            }

            val idToken = account.idToken
            if (idToken == null) {
                _authState.value = AuthState.Error("Failed to get Google ID token")
                return@launch
            }

            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Success(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun handleGoogleSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                signInWithGoogle(account)
            } catch (e: ApiException) {
                _authState.value = AuthState.Error("Google sign in failed: ${e.message}")
            }
        }
    }

    fun getGoogleSignInClient(context: Context) = authRepository.getGoogleSignInClient()

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        contactNumber: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.signUp(email, password, firstName, lastName, username, contactNumber)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Success(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    // New extended signup method
    fun signUpExtended(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        contactNumber: String,
        gender: String,
        dateOfBirth: Long,
        profileImageUrl: String = ""
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.signUpExtended(
                email, password, firstName, lastName, username, contactNumber, gender, dateOfBirth, profileImageUrl
            )) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Success(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Result.Success -> {
                    _authState.value = AuthState.Idle
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.sendEmailVerification()) {
                is Result.Success -> {
                    _authState.value = AuthState.Idle
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun clearState() {
        _authState.value = AuthState.Idle
    }
}