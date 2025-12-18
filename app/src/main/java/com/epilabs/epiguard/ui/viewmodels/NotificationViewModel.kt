package com.epilabs.epiguard.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epilabs.epiguard.data.repo.NotificationRepository
import com.epilabs.epiguard.models.AppNotification
import com.epilabs.epiguard.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val notificationRepository = NotificationRepository()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = notificationRepository.getNotifications()) {
                is Result.Success -> {
                    _notifications.value = result.data
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is Result.Success -> {
                    _unreadCount.value = result.data
                }
                is Result.Error -> {
                    // Don't set error for unread count failure
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (val result = notificationRepository.markAsRead(notificationId)) {
                is Result.Success -> {
                    loadNotifications()
                    loadUnreadCount()
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            when (val result = notificationRepository.deleteNotification(notificationId)) {
                is Result.Success -> {
                    loadNotifications()
                    loadUnreadCount()
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}