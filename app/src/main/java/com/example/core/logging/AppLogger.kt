package com.example.core.logging

import android.util.Log

/**
 * Structured application logger for Campus Plug SA.
 * Provides uniform tagging, breadcrumbs, and diagnostic output across Debug and Release builds.
 */
object AppLogger {
    private const val DEFAULT_TAG = "CampusPlug"

    var isDebug: Boolean = true

    fun d(message: String, tag: String = DEFAULT_TAG) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }

    fun i(message: String, tag: String = DEFAULT_TAG) {
        Log.i(tag, message)
    }

    fun w(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    /**
     * Log structured diagnostic events (e.g. Firebase lifecycle, auth events)
     */
    fun logEvent(event: String, parameters: Map<String, Any?> = emptyMap(), tag: String = DEFAULT_TAG) {
        val payload = parameters.entries.joinToString(prefix = "{", postfix = "}") {
            "\"${it.key}\": \"${it.value}\""
        }
        i("EVENT: $event -> $payload", tag)
    }
}
