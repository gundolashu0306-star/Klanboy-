package com.example.core.common

/**
 * Standard Jetpack Compose UI state representation.
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Empty(val message: String = "No data available") : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
