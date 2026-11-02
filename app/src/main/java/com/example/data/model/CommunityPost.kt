package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Community Discussion Post model for Campus Plug SA.
 * Stored in Firestore subcollection: /communities/{communityId}/posts/{postId}
 */
@IgnoreExtraProperties
data class CommunityPost(
    @DocumentId
    val id: String = "",
    val communityId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val title: String = "",
    val content: String = "",
    val likesCount: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun isLikedBy(userId: String?): Boolean =
        userId != null && likedBy.contains(userId)
}
