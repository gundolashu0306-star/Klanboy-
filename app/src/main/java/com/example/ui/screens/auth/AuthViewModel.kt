package com.example.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Result
import com.example.core.common.UiState
import com.example.core.logging.AppLogger
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirestoreService
import com.example.data.model.SouthAfricanCampus
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FirebaseUser?>>(UiState.Idle)
    val uiState: StateFlow<UiState<FirebaseUser?>> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow(authRepository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _passwordResetSent = MutableStateFlow(false)
    val passwordResetSent: StateFlow<Boolean> = _passwordResetSent.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _currentUser.value = user
                if (user != null && _uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Success(user)
                }
            }
        }
    }

    /**
     * Complete Student Registration with Email/Password & Profile Initialization
     */
    fun registerStudent(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        institutionName: String,
        campusName: String,
        campusCode: String,
        studentNumber: String,
        course: String,
        yearOfStudy: Int
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        val trimmedCourse = course.trim()
        val trimmedStudentNumber = studentNumber.trim()

        // Validation
        if (trimmedName.length < 2) {
            _uiState.value = UiState.Error("Please enter your full legal or preferred name.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = UiState.Error("Please enter a valid student or personal email address.")
            return
        }
        if (password.length < 6) {
            _uiState.value = UiState.Error("Password must be at least 6 characters.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = UiState.Error("Passwords do not match. Please re-enter carefully.")
            return
        }
        if (trimmedCourse.isBlank()) {
            _uiState.value = UiState.Error("Please specify your course, degree or qualification.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val authResult = authRepository.signUpWithEmail(trimmedEmail, password, trimmedName)) {
                is Result.Success -> {
                    val user = authResult.data
                    val profile = UserProfile(
                        id = user.uid,
                        email = trimmedEmail,
                        displayName = trimmedName,
                        institutionName = institutionName.ifBlank { "University of Johannesburg" },
                        campusCode = campusCode.ifBlank { SouthAfricanCampus.UJ.code },
                        campusName = campusName.ifBlank { "Main Campus" },
                        studentNumber = trimmedStudentNumber,
                        course = trimmedCourse,
                        degree = trimmedCourse,
                        yearOfStudy = yearOfStudy.coerceIn(1, 7),
                        verificationStatus = VerificationStatus.NOT_SUBMITTED.name,
                        isStudentVerified = false,
                        isAdmin = false
                    )

                    when (val saveResult = firestoreService.saveUserProfile(profile)) {
                        is Result.Success -> {
                            AppLogger.i("Student registration & profile complete for UID: ${user.uid}")
                            _uiState.value = UiState.Success(user)
                        }
                        is Result.Error -> {
                            AppLogger.w("Auth created but Firestore profile save failed: ${saveResult.error.userMessage}")
                            // Still success with user so session persists
                            _uiState.value = UiState.Success(user)
                        }
                        is Result.Loading -> {}
                    }
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(authResult.error.userMessage)
                }
                is Result.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    /**
     * Secure Login with Email and Password
     */
    fun loginWithEmail(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = UiState.Error("Please enter a valid email address.")
            return
        }
        if (password.isBlank()) {
            _uiState.value = UiState.Error("Please enter your password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.signInWithEmail(trimmedEmail, password)) {
                is Result.Success -> {
                    AppLogger.i("Student login successful: ${result.data.email}")
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    /**
     * Password Recovery
     */
    fun sendPasswordReset(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = UiState.Error("Please enter a valid email address to receive password reset link.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.sendPasswordResetEmail(trimmedEmail)) {
                is Result.Success -> {
                    _passwordResetSent.value = true
                    _uiState.value = UiState.Idle
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Interactive Google Sign-In
     */
    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.signInWithGoogle(activity)) {
                is Result.Success -> {
                    val user = result.data
                    // Check if profile exists, if not create baseline profile
                    val profileCheck = firestoreService.getUserProfile(user.uid)
                    if (profileCheck is Result.Success && profileCheck.data == null) {
                        val newProfile = UserProfile(
                            id = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: "Student",
                            photoUrl = user.photoUrl?.toString() ?: "",
                            verificationStatus = VerificationStatus.NOT_SUBMITTED.name,
                            isStudentVerified = false,
                            isAdmin = false
                        )
                        firestoreService.saveUserProfile(newProfile)
                    }
                    _uiState.value = UiState.Success(user)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    fun dismissError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }

    fun resetPasswordState() {
        _passwordResetSent.value = false
    }
}
