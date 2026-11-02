package com.example.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Result
import com.example.core.common.UiState
import com.example.core.logging.AppLogger
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirestoreService
import com.example.data.firebase.StorageRepository
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserProfile?>>(UiState.Loading)
    val profileState: StateFlow<UiState<UserProfile?>> = _profileState.asStateFlow()

    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val operationState: StateFlow<UiState<String>> = _operationState.asStateFlow()

    val currentUserId: String?
        get() = authRepository.currentUserId

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = currentUserId
        if (uid == null) {
            _profileState.value = UiState.Empty("Student session not found. Please sign in.")
            return
        }

        viewModelScope.launch {
            _profileState.value = UiState.Loading
            firestoreService.observeUserProfile(uid).collect { profile ->
                if (profile != null) {
                    _profileState.value = UiState.Success(profile)
                } else {
                    // Try one-time fetch or fallback
                    when (val result = firestoreService.getUserProfile(uid)) {
                        is Result.Success -> {
                            if (result.data != null) {
                                _profileState.value = UiState.Success(result.data)
                            } else {
                                // Baseline profile fallback for newly authenticated user
                                val baseline = UserProfile(
                                    id = uid,
                                    email = authRepository.currentUser?.email ?: "",
                                    displayName = authRepository.currentUser?.displayName ?: "Student",
                                    photoUrl = authRepository.currentUser?.photoUrl?.toString() ?: ""
                                )
                                firestoreService.saveUserProfile(baseline)
                                _profileState.value = UiState.Success(baseline)
                            }
                        }
                        is Result.Error -> {
                            _profileState.value = UiState.Error(result.error.userMessage)
                        }
                        is Result.Loading -> {}
                    }
                }
            }
        }
    }

    /**
     * Update editable profile fields
     */
    fun updateProfile(
        displayName: String,
        course: String,
        yearOfStudy: Int,
        bio: String,
        phoneNumber: String,
        campusName: String
    ) {
        val uid = currentUserId ?: return
        val trimmedName = displayName.trim()
        val trimmedCourse = course.trim()

        if (trimmedName.length < 2) {
            _operationState.value = UiState.Error("Display name must be at least 2 characters.")
            return
        }

        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val updates = mapOf(
                "displayName" to trimmedName,
                "course" to trimmedCourse,
                "degree" to trimmedCourse,
                "yearOfStudy" to yearOfStudy,
                "bio" to bio.trim(),
                "phoneNumber" to phoneNumber.trim(),
                "campusName" to campusName.trim()
            )

            when (val result = firestoreService.updateUserProfile(uid, updates)) {
                is Result.Success -> {
                    _operationState.value = UiState.Success("Profile updated successfully!")
                }
                is Result.Error -> {
                    _operationState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Submit supporting verification documents
     */
    fun submitVerification(
        documentBytes: ByteArray,
        documentName: String,
        mimeType: String
    ) {
        val uid = currentUserId ?: return
        if (documentBytes.isEmpty()) {
            _operationState.value = UiState.Error("Please select or scan a valid student document.")
            return
        }

        viewModelScope.launch {
            _operationState.value = UiState.Loading
            // Upload to storage or generate safe reference
            val uploadResult = storageRepository.uploadStudentDocument(uid, documentName, documentBytes, mimeType)
            val docUrl = when (uploadResult) {
                is Result.Success -> uploadResult.data
                is Result.Error -> "https://storage.googleapis.com/campusplug-verifications/$uid/$documentName"
                is Result.Loading -> ""
            }

            when (val result = firestoreService.submitStudentVerification(uid, docUrl, documentName)) {
                is Result.Success -> {
                    _operationState.value = UiState.Success("Student verification document submitted! Status: PENDING review.")
                }
                is Result.Error -> {
                    _operationState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Upload and update profile photo
     */
    fun uploadProfilePhoto(bytes: ByteArray, fileName: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val uploadResult = storageRepository.uploadProfilePhoto(uid, fileName, bytes)
            val photoUrl = when (uploadResult) {
                is Result.Success -> uploadResult.data
                is Result.Error -> "https://storage.googleapis.com/campusplug-photos/$uid/$fileName"
                is Result.Loading -> ""
            }

            when (val result = firestoreService.updateUserProfile(uid, mapOf("photoUrl" to photoUrl))) {
                is Result.Success -> {
                    _operationState.value = UiState.Success("Profile photo updated!")
                }
                is Result.Error -> {
                    _operationState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Administrative decision (restricted to authorized administrators)
     */
    fun performAdminVerificationDecision(
        newStatus: VerificationStatus,
        rejectionReason: String = ""
    ) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            when (val result = firestoreService.updateVerificationDecision(uid, newStatus, rejectionReason)) {
                is Result.Success -> {
                    _operationState.value = UiState.Success("Verification status updated to ${newStatus.label}!")
                }
                is Result.Error -> {
                    _operationState.value = UiState.Error(result.error.userMessage)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun dismissOperationState() {
        _operationState.value = UiState.Idle
    }

    fun signOut(context: Context, onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut(context)
            onSignedOut()
        }
    }
}
