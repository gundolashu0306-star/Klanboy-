package com.example.core.error

/**
 * Domain-specific errors for Campus Plug SA.
 * Clean sealed hierarchy preventing unhandled exceptions and providing friendly messages.
 */
sealed class AppError(open val userMessage: String) {
    data class Network(
        override val userMessage: String = "No internet connection. Please check your data or Wi-Fi.",
        val isOffline: Boolean = true
    ) : AppError(userMessage)

    data class Auth(
        override val userMessage: String,
        val code: String = "AUTH_ERROR",
        val isCancelled: Boolean = false
    ) : AppError(userMessage)

    data class Firestore(
        override val userMessage: String = "Unable to sync with campus cloud. Please try again.",
        val operation: String = "UNKNOWN",
        val path: String = "",
        val errorCode: String = ""
    ) : AppError(userMessage)

    data class Storage(
        override val userMessage: String = "Unable to process upload. Please check file and retry.",
        val operation: String = "UPLOAD",
        val path: String = ""
    ) : AppError(userMessage)

    data class Configuration(
        override val userMessage: String,
        val missingService: String
    ) : AppError(userMessage)

    data class Validation(
        override val userMessage: String,
        val field: String
    ) : AppError(userMessage)

    data class Unknown(
        override val userMessage: String = "An unexpected error occurred. Please try again.",
        val cause: Throwable? = null
    ) : AppError(userMessage)
}
