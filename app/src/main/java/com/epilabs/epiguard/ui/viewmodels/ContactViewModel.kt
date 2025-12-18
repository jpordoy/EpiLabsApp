package com.epilabs.epiguard.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epilabs.epiguard.data.repo.ContactRepository
import com.epilabs.epiguard.data.repo.UserRepository
import com.epilabs.epiguard.models.Contact
import com.epilabs.epiguard.models.User
import com.epilabs.epiguard.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    private val contactRepository = ContactRepository()
    private val userRepository = UserRepository()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = contactRepository.getContacts()) {
                is Result.Success -> {
                    _contacts.value = result.data
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = contactRepository.addContact(contact)) {
                is Result.Success -> {
                    loadContacts() // Reload to get the updated list
                }
                is Result.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = contactRepository.updateContact(contact)) {
                is Result.Success -> {
                    loadContacts()
                }
                is Result.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
            }
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = contactRepository.deleteContact(contactId)) {
                is Result.Success -> {
                    loadContacts()
                }
                is Result.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
            }
        }
    }

    fun linkContactToUser(contactId: String, userEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // First check if user exists
            when (val userResult = userRepository.findUserByEmail(userEmail)) {
                is Result.Success -> {
                    if (userResult.data != null) {
                        when (val result = contactRepository.linkContactToUser(contactId, userEmail)) {
                            is Result.Success -> {
                                loadContacts()
                            }
                            is Result.Error -> {
                                _error.value = result.message
                                _isLoading.value = false
                            }
                        }
                    } else {
                        _error.value = "User not found with email: $userEmail"
                        _isLoading.value = false
                    }
                }
                is Result.Error -> {
                    _error.value = userResult.message
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}