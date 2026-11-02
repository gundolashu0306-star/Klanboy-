package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Entity representing a student comment or reply to a feed post.
 * Stored in subcollection /posts/{postId}/comments/{commentId}
 */
@IgnoreExtraProperties
data class FeedComment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val content: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
