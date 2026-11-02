package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Campus Community model for Campus Plug SA.
 * Represents societies, faculty groups, sports clubs, and study circles.
 */
@IgnoreExtraProperties
data class Community(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "ACADEMIC",
    val campusCode: String = SouthAfricanCampus.UJ.code,
    val institutionName: String = "University of Johannesburg",
    val campusName: String = "Auckland Park Kingsway",
    val imageUrl: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val memberCount: Long = 1L,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun isMember(userId: String?): Boolean =
        userId != null && (memberUids.contains(userId) || creatorId == userId)
}
