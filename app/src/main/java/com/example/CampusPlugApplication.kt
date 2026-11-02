package com.example

import android.app.Application
import com.example.core.logging.AppLogger

/**
 * Main application class for Campus Plug SA.
 * Configures central logging, lifecycle telemetry, and handles uncaught exceptions.
 */
class CampusPlugApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        AppLogger.isDebug = BuildConfig.DEBUG
        AppLogger.i("Campus Plug SA initialized. Application ID: ${BuildConfig.APPLICATION_ID}, Version: ${BuildConfig.VERSION_NAME}")

        // Global uncaught exception handler for crash resilience and breadcrumbs
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e("FATAL UNCAUGHT EXCEPTION on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
