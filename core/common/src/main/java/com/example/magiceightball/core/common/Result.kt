package com.example.magiceightball.core.common

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
}

fun <T> Result<T>.exceptionOrNull(): Throwable? = when (this) {
    is Result.Success -> null
    is Result.Error -> exception
}
