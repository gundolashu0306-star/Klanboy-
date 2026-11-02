package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Entity representing a student broadcast post in the Realtime Student Feed.
 * Stored in top-level Firestore collection /posts/{postId}
 */
@IgnoreExtraProperties
data class FeedPost(
    @DocumentId
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val authorCampus: String = "",
    val isAuthorVerified: Boolean = false,
    val content: String = "",
    val imageUrl: String = "",
    val likesCount: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
