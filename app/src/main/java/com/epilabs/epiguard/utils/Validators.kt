package com.epilabs.epiguard.utils

object Validators {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotBlank() && phone.length >= 10
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }

    fun isValidUsername(username: String): Boolean {
        return username.isNotBlank() && username.length >= 3 && username.all { it.isLetterOrDigit() || it == '_' }
    }
}