package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Verified student profile model for Campus Plug SA.
 * Stored in Firestore collection: /users/{userId}
 *
 * All properties provide default values for Firestore reflection deserialization.
 */
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val institutionName: String = "University of Johannesburg",
    val campusCode: String = SouthAfricanCampus.UJ.code,
    val campusName: String = "Auckland Park Kingsway",
    val studentNumber: String = "",
    val course: String = "",
    val degree: String = "", // Alias / backward-compatible
    val yearOfStudy: Int = 1,
    val bio: String = "",
    val phoneNumber: String = "",
    val verificationStatus: String = VerificationStatus.NOT_SUBMITTED.name,
    val verificationDocumentUrl: String = "",
    val verificationDocumentName: String = "",
    val verificationSubmittedAt: Timestamp? = null,
    val verificationRejectionReason: String = "",
    val isStudentVerified: Boolean = false, // Derived or synced with VERIFIED status
    val isAdmin: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    val status: VerificationStatus
        get() = VerificationStatus.fromString(verificationStatus)

    val effectiveCourse: String
        get() = course.ifBlank { degree }

    val isVerified: Boolean
        get() = status == VerificationStatus.VERIFIED || isStudentVerified
}
