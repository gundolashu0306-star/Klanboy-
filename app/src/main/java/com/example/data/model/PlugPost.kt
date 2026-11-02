package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Core entity for user-authored student posts, marketplace listings, or accommodation ads.
 * Stored under /campuses/{campusCode}/plugs/{plugId}
 */
@IgnoreExtraProperties
data class PlugPost(
    @DocumentId
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorCampus: String = "",
    val isAuthorVerified: Boolean = false,
    val category: String = CommunityCategory.CAMPUS_PULSE.name,
    val title: String = "",
    val content: String = "",
    val priceZar: Double? = null,
    val contactWhatsapp: String = "",
    val imageUrls: List<String> = emptyList(),
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
