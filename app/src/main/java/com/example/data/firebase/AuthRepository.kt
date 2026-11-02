package com.example.data.firebase

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.core.common.Result
import com.example.core.error.AppError
import com.example.core.logging.AppLogger
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Interface defining authentication contracts for Campus Plug SA.
 * Supports Email/Password registration, Login, Password Reset, and Google Sign-In.
 */
interface AuthRepository {
    val currentUser: FirebaseUser?
    val currentUserId: String?
    val isAuthenticated: Boolean

    fun observeAuthState(): Flow<FirebaseUser?>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser>
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser>
    suspend fun signOut(context: Context): Result<Unit>
    fun isBackendConfigured(context: Context): Boolean
}

class FirebaseAuthRepository : AuthRepository {

    private val auth: FirebaseAuth
        get() = Firebase.auth

    override val currentUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

    override val currentUserId: String?
        get() = currentUser?.uid

    override val isAuthenticated: Boolean
        get() = currentUser != null

    override fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        try {
            auth.addAuthStateListener(listener)
        } catch (e: Exception) {
            AppLogger.w("Unable to attach AuthStateListener: ${e.message}")
            trySend(null)
        }
        awaitClose {
            try {
                auth.removeAuthStateListener(listener)
            } catch (e: Exception) {
                AppLogger.w("Unable to remove AuthStateListener: ${e.message}")
            }
        }
    }

    override fun isBackendConfigured(context: Context): Boolean {
        return try {
            val clientId = context.getString(R.string.default_web_client_id)
            clientId.isNotBlank() && clientId != "UNCONFIGURED_WEB_CLIENT_ID"
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            AppLogger.i("Attempting student registration with email: $email")
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
                ?: return Result.Error(AppError.Auth("Registration completed without student session."))

            // Update Firebase Auth profile display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                AppLogger.w("Could not set display name on auth token: ${e.message}")
            }

            AppLogger.i("Student registered successfully with UID: ${user.uid}")
            Result.Success(user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            AppLogger.w("Registration failed - weak password: ${e.message}")
            Result.Error(AppError.Auth("Password is too weak. Please use at least 6 characters.", code = "WEAK_PASSWORD"))
        } catch (e: FirebaseAuthUserCollisionException) {
            AppLogger.w("Registration failed - email already in use: ${e.message}")
            Result.Error(AppError.Auth("An account already exists with this email address. Please sign in instead.", code = "EMAIL_IN_USE"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AppLogger.w("Registration failed - invalid credentials: ${e.message}")
            Result.Error(AppError.Auth("Please enter a valid email address.", code = "INVALID_EMAIL"))
        } catch (e: Exception) {
            AppLogger.e("Registration error: ${e.message}", e)
            Result.Error(AppError.Auth(e.localizedMessage ?: "Failed to register student account."))
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            AppLogger.i("Attempting student login with email: $email")
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
                ?: return Result.Error(AppError.Auth("Authentication succeeded without user session."))

            AppLogger.i("Student logged in successfully with UID: ${user.uid}")
            Result.Success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            AppLogger.w("Login failed - user not found or disabled: ${e.message}")
            Result.Error(AppError.Auth("No student account found with this email. Please register first.", code = "USER_NOT_FOUND"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AppLogger.w("Login failed - invalid password or credentials: ${e.message}")
            Result.Error(AppError.Auth("Incorrect email or password. Please try again.", code = "WRONG_PASSWORD"))
        } catch (e: Exception) {
            AppLogger.e("Login error: ${e.message}", e)
            Result.Error(AppError.Auth(e.localizedMessage ?: "Failed to sign in. Please check your network and credentials."))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            AppLogger.i("Sending password reset email to: $email")
            auth.sendPasswordResetEmail(email.trim()).await()
            AppLogger.i("Password reset email dispatched successfully.")
            Result.Success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            AppLogger.w("Password reset failed - user not found: ${e.message}")
            Result.Error(AppError.Auth("No student account found with this email address.", code = "USER_NOT_FOUND"))
        } catch (e: Exception) {
            AppLogger.e("Password reset error: ${e.message}", e)
            Result.Error(AppError.Auth(e.localizedMessage ?: "Failed to send password reset email."))
        }
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        val clientId = try {
            val id = activity.getString(R.string.default_web_client_id)
            if (id.isBlank() || id == "UNCONFIGURED_WEB_CLIENT_ID") {
                return Result.Error(
                    AppError.Configuration(
                        userMessage = "Google Sign-In Web Client ID is not configured.",
                        missingService = "Google Sign-In Web Client ID"
                    )
                )
            }
            id
        } catch (e: Exception) {
            return Result.Error(
                AppError.Configuration(
                    userMessage = "Google Services configuration is not yet active in this build.",
                    missingService = "default_web_client_id"
                )
            )
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            AppLogger.i("Initiating interactive Google Sign-In with Credential Manager...")
            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    AppLogger.i("Google Sign-In successful for student UID: ${user.uid}")
                    Result.Success(user)
                } else {
                    Result.Error(AppError.Auth("Firebase sign-in completed without user profile."))
                }
            } else {
                AppLogger.w("Unexpected credential type: ${credential.type}")
                Result.Error(AppError.Auth("Received unexpected credential type from Google Play services."))
            }
        } catch (e: GetCredentialCancellationException) {
            AppLogger.w("Google Sign-In flow was cancelled or dismissed: ${e.message}", e)
            Result.Error(AppError.Auth(userMessage = "Sign-in was cancelled.", isCancelled = true))
        } catch (e: Exception) {
            AppLogger.e("Google Sign-In error: ${e.message}", e)
            Result.Error(AppError.Auth(userMessage = e.localizedMessage ?: "Sign in failed."))
        }
    }

    override suspend fun signOut(context: Context): Result<Unit> {
        return try {
            auth.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            AppLogger.i("Student session signed out successfully.")
            Result.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Sign-out error: ${e.message}", e)
            Result.Error(AppError.Unknown(cause = e))
        }
    }
}
