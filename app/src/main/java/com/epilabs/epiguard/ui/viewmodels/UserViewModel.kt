package com.epilabs.epiguard.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epilabs.epiguard.data.repo.UserRepository
import com.epilabs.epiguard.models.User
import com.epilabs.epiguard.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // New state for email existence check
    private val _emailExists = MutableStateFlow<Boolean?>(null)
    val emailExists: StateFlow<Boolean?> = _emailExists.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = userRepository.getCurrentUser()) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = userRepository.updateUser(user)) {
                is Result.Success -> {
                    _currentUser.value = user
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun uploadProfileImage(imageStream: InputStream) {
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            _isLoading.value = true

            when (val result = userRepository.uploadProfileImage(user.userId, imageStream)) {
                is Result.Success -> {
                    val updatedUser = user.copy(profileImageUrl = result.data)
                    updateUser(updatedUser)
                }
                is Result.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
            }
        }
    }

    // New method to check if email exists
    fun checkEmailExists(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _emailExists.value = null

            when (val result = userRepository.findUserByEmail(email)) {
                is Result.Success -> {
                    _emailExists.value = result.data != null
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                    _emailExists.value = null
                }
            }

            _isLoading.value = false
        }
    }

    // Method to upload profile image and return URL (for signup flow)
    suspend fun uploadProfileImageAndGetUrl(userId: String, imageStream: InputStream): Result<String> {
        return userRepository.uploadProfileImage(userId, imageStream)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearEmailExistsState() {
        _emailExists.value = null
    }
}