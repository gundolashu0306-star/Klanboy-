package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.core.logging.AppLogger
import com.google.firebase.FirebaseApp

/**
 * Diagnostic model describing the backend readiness of Campus Plug SA.
 */
data class FirebaseReadinessReport(
    val isFirebaseInitialized: Boolean,
    val isGoogleServicesBound: Boolean,
    val isGoogleSignInReady: Boolean,
    val isFirestoreReady: Boolean,
    val isStorageReady: Boolean,
    val webClientId: String,
    val firestoreDatabaseId: String,
    val missingRequirements: List<String>
) {
    val isFullyReady: Boolean
        get() = isFirebaseInitialized && isGoogleSignInReady && isFirestoreReady
}

object FirebaseConfig {

    fun evaluateReadiness(context: Context): FirebaseReadinessReport {
        val missing = mutableListOf<String>()

        // 1. Core Firebase Initialization check
        val isFirebaseInit = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            AppLogger.w("FirebaseApp check failed: ${e.message}")
            false
        }
        if (!isFirebaseInit) {
            missing.add("FirebaseApp has not been initialized. Awaiting google-services.json provisioning.")
        }

        // 2. Google Services Plugin Web Client ID resolution
        var webClientId = "UNCONFIGURED"
        var isGoogleSignInConfigured = false
        try {
            webClientId = context.getString(R.string.default_web_client_id)
            if (webClientId.isNotBlank() && webClientId != "UNCONFIGURED_WEB_CLIENT_ID") {
                isGoogleSignInConfigured = true
            } else {
                missing.add("Google Web Client ID is unconfigured (requires Google Sign-In backend provisioning).")
            }
        } catch (e: Exception) {
            missing.add("Resource R.string.default_web_client_id not materialized by google-services plugin.")
        }

        // 3. Firestore Database ID resolution
        var firestoreDbId = "(default)"
        var isFirestoreConfigured = false
        try {
            firestoreDbId = context.getString(R.string.firestore_database_id)
            isFirestoreConfigured = firestoreDbId.isNotBlank()
        } catch (e: Exception) {
            missing.add("Firestore Database ID is missing.")
        }

        // 4. Cloud Storage Readiness
        val isStorageConfigured = isFirebaseInit

        AppLogger.i("Evaluated Firebase readiness: isInit=$isFirebaseInit, authReady=$isGoogleSignInConfigured, firestoreReady=$isFirestoreConfigured")

        return FirebaseReadinessReport(
            isFirebaseInitialized = isFirebaseInit,
            isGoogleServicesBound = isGoogleSignInConfigured,
            isGoogleSignInReady = isGoogleSignInConfigured,
            isFirestoreReady = isFirestoreConfigured,
            isStorageReady = isStorageConfigured,
            webClientId = webClientId,
            firestoreDatabaseId = firestoreDbId,
            missingRequirements = missing
        )
    }
}
