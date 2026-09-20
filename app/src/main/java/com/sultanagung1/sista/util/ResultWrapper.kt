package com.sultanagung1.sista.util

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()
    data class Error(val exception: Exception, val message: String = exception.localizedMessage ?: "Unknown Error") : ResultWrapper<Nothing>()
    object Loading : ResultWrapper<Nothing>()
}
