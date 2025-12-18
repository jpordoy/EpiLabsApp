package com.epilabs.epiguard.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): Result<T> {
        if (this is Error) action(message)
        return this
    }
}