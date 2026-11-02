package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.core.common.Result
import com.example.core.error.AppError
import com.example.core.logging.AppLogger
import com.example.data.model.Community
import com.example.data.model.CommunityPost
import com.example.data.model.FeedComment
import com.example.data.model.FeedPost
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Service managing Firestore database instances, security mappings,
 * and student profile operations for Campus Plug SA.
 * Enforces mandatory custom database ID binding via R.string.firestore_database_id.
 */
class FirestoreService(private val context: Context) {

    val firestoreDatabaseId: String
        get() = try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isBlank()) "(default)" else dbId
        } catch (e: Exception) {
            "(default)"
        }

    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance(firestoreDatabaseId)

    fun handleFirestoreError(
        exception: Exception,
        operation: String,
        path: String
    ): AppError.Firestore {
        val errorCode = if (exception is FirebaseFirestoreException) {
            exception.code.name
        } else {
            "UNKNOWN"
        }

        AppLogger.e(
            "Firestore Error [op=$operation, path=$path, code=$errorCode]: ${exception.message}",
            exception
        )

        val userMessage = when (errorCode) {
            "PERMISSION_DENIED" -> "Access denied. Operation restricted by campus security rules."
            "NOT_FOUND" -> "The requested student record was not found."
            "ALREADY_EXISTS" -> "This student record already exists."
            "RESOURCE_EXHAUSTED" -> "System is experiencing high traffic. Please retry in a moment."
            "UNAVAILABLE" -> "Campus cloud services are temporarily unavailable."
            else -> exception.localizedMessage ?: "Failed to perform database operation."
        }

        return AppError.Firestore(
            userMessage = userMessage,
            operation = operation,
            path = path,
            errorCode = errorCode
        )
    }

    /**
     * Fetch user profile by student UID
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val profile = snapshot.toObject(UserProfile::class.java)
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "GET", docRef.path))
        }
    }

    /**
     * Realtime observation of student profile
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot -> snapshot.toObject(UserProfile::class.java) }
            .catch { e ->
                AppLogger.w("Error observing user profile: ${e.message}")
                emit(null)
            }
    }

    /**
     * Create initial student profile on registration
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val docRef = db.collection("users").document(profile.id)
        return try {
            docRef.set(profile).await()
            AppLogger.i("Student profile created for UID: ${profile.id}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "SET", docRef.path))
        }
    }

    /**
     * Update permitted profile fields
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any?>): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val payload = updates + mapOf("updatedAt" to FieldValue.serverTimestamp())
            docRef.update(payload).await()
            AppLogger.i("Student profile updated for UID: $userId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    /**
     * Submit supporting verification documents
     * Automatically transitions status to PENDING
     */
    suspend fun submitStudentVerification(
        userId: String,
        documentUrl: String,
        documentName: String
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        val payload = mapOf(
            "verificationStatus" to VerificationStatus.PENDING.name,
            "verificationDocumentUrl" to documentUrl,
            "verificationDocumentName" to documentName,
            "verificationSubmittedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            AppLogger.i("Student verification submitted for UID: $userId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    /**
     * Restricted verification decision (Simulated / Authorised Administrator)
     */
    suspend fun updateVerificationDecision(
        userId: String,
        newStatus: VerificationStatus,
        rejectionReason: String = ""
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        val isVerified = newStatus == VerificationStatus.VERIFIED
        val payload = mapOf(
            "verificationStatus" to newStatus.name,
            "isStudentVerified" to isVerified,
            "verificationRejectionReason" to rejectionReason,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            AppLogger.i("Verification decision recorded for UID $userId: $newStatus")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    // ==========================================
    // Phase 2.2: Campus Communities & Discussions
    // ==========================================

    /**
     * Real-time stream of communities, optionally filtered by campus code
     */
    fun observeCommunities(campusCode: String? = null): Flow<List<Community>> {
        val collection = db.collection("communities")
        val query = if (campusCode.isNullOrBlank()) {
            collection
        } else {
            collection.whereEqualTo("campusCode", campusCode)
        }
        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(Community::class.java) }
            .catch { e ->
                AppLogger.w("Error observing communities: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Real-time stream of communities that the given student has joined
     */
    fun observeJoinedCommunities(userId: String): Flow<List<Community>> {
        val query = db.collection("communities").whereArrayContains("memberUids", userId)
        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(Community::class.java) }
            .catch { e ->
                AppLogger.w("Error observing joined communities: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Real-time stream of a single community's details
     */
    fun observeCommunity(communityId: String): Flow<Community?> {
        val docRef = db.collection("communities").document(communityId)
        return docRef.snapshots()
            .map { snapshot -> snapshot.toObject(Community::class.java) }
            .catch { e ->
                AppLogger.w("Error observing community $communityId: ${e.message}")
                emit(null)
            }
    }

    /**
     * Fetch a community by ID
     */
    suspend fun getCommunity(communityId: String): Result<Community?> {
        val docRef = db.collection("communities").document(communityId)
        return try {
            val snapshot = docRef.get().await()
            Result.Success(snapshot.toObject(Community::class.java))
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "GET", docRef.path))
        }
    }

    /**
     * Create a new student community
     */
    suspend fun createCommunity(community: Community): Result<String> {
        val docRef = db.collection("communities").document()
        val toSave = community.copy(
            id = docRef.id,
            memberUids = if (community.memberUids.isEmpty() && community.creatorId.isNotBlank()) {
                listOf(community.creatorId)
            } else community.memberUids,
            memberCount = if (community.memberCount <= 0L) 1L else community.memberCount
        )
        return try {
            docRef.set(toSave).await()
            AppLogger.i("Created community ${toSave.name} (ID: ${docRef.id})")
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "CREATE", docRef.path))
        }
    }

    /**
     * Join a community atomically
     */
    suspend fun joinCommunity(communityId: String, userId: String): Result<Unit> {
        val docRef = db.collection("communities").document(communityId)
        return try {
            docRef.update(
                mapOf(
                    "memberUids" to FieldValue.arrayUnion(userId),
                    "memberCount" to FieldValue.increment(1)
                )
            ).await()
            AppLogger.i("User $userId joined community $communityId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    /**
     * Leave a community atomically
     */
    suspend fun leaveCommunity(communityId: String, userId: String): Result<Unit> {
        val docRef = db.collection("communities").document(communityId)
        return try {
            docRef.update(
                mapOf(
                    "memberUids" to FieldValue.arrayRemove(userId),
                    "memberCount" to FieldValue.increment(-1)
                )
            ).await()
            AppLogger.i("User $userId left community $communityId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    /**
     * Real-time stream of discussion posts within a community
     */
    fun observeCommunityPosts(communityId: String): Flow<List<CommunityPost>> {
        val query = db.collection("communities")
            .document(communityId)
            .collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(CommunityPost::class.java) }
            .catch { e ->
                AppLogger.w("Error observing posts in community $communityId: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Post a new topic or discussion in the community
     */
    suspend fun createCommunityPost(communityId: String, post: CommunityPost): Result<String> {
        val docRef = db.collection("communities")
            .document(communityId)
            .collection("posts")
            .document()

        val toSave = post.copy(id = docRef.id, communityId = communityId)
        return try {
            docRef.set(toSave).await()
            AppLogger.i("Published post ${docRef.id} in community $communityId")
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "CREATE", docRef.path))
        }
    }

    /**
     * Toggle like/unlike on a community discussion post
     */
    suspend fun togglePostLike(
        communityId: String,
        postId: String,
        userId: String,
        isCurrentlyLiked: Boolean
    ): Result<Unit> {
        val docRef = db.collection("communities")
            .document(communityId)
            .collection("posts")
            .document(postId)

        return try {
            if (isCurrentlyLiked) {
                docRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayRemove(userId),
                        "likesCount" to FieldValue.increment(-1)
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayUnion(userId),
                        "likesCount" to FieldValue.increment(1)
                    )
                ).await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    // ==========================================
    // Phase 2.3: Realtime Student Feed
    // ==========================================

    /**
     * Real-time stream of student feed posts, ordered chronologically (newest first)
     */
    fun observeFeedPosts(): Flow<List<FeedPost>> {
        val query = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(FeedPost::class.java) }
            .catch { e ->
                AppLogger.w("Error observing realtime feed: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Create a new student feed post (text and optional image)
     */
    suspend fun createFeedPost(post: FeedPost): Result<String> {
        val docRef = db.collection("posts").document()
        val toSave = post.copy(id = docRef.id)
        return try {
            docRef.set(toSave).await()
            AppLogger.i("Feed post published: ${docRef.id} by ${post.authorName}")
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "CREATE", docRef.path))
        }
    }

    /**
     * Delete a feed post
     */
    suspend fun deleteFeedPost(postId: String): Result<Unit> {
        val docRef = db.collection("posts").document(postId)
        return try {
            docRef.delete().await()
            AppLogger.i("Feed post deleted: $postId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "DELETE", docRef.path))
        }
    }

    /**
     * Toggle like/unlike on a student feed post
     */
    suspend fun toggleFeedPostLike(
        postId: String,
        userId: String,
        isCurrentlyLiked: Boolean
    ): Result<Unit> {
        val docRef = db.collection("posts").document(postId)
        return try {
            if (isCurrentlyLiked) {
                docRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayRemove(userId),
                        "likesCount" to FieldValue.increment(-1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayUnion(userId),
                        "likesCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "UPDATE", docRef.path))
        }
    }

    /**
     * Real-time stream of comments for a given feed post
     */
    fun observePostComments(postId: String): Flow<List<FeedComment>> {
        val query = db.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(FeedComment::class.java) }
            .catch { e ->
                AppLogger.w("Error observing comments for post $postId: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Add a comment to a feed post and atomically increment post commentsCount
     */
    suspend fun addPostComment(postId: String, comment: FeedComment): Result<String> {
        val postRef = db.collection("posts").document(postId)
        val commentRef = postRef.collection("comments").document()
        val toSave = comment.copy(id = commentRef.id, postId = postId)

        return try {
            commentRef.set(toSave).await()
            // Increment post comments counter
            postRef.update(
                mapOf(
                    "commentsCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            AppLogger.i("Added comment ${commentRef.id} to post $postId")
            Result.Success(commentRef.id)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "CREATE", commentRef.path))
        }
    }

    /**
     * Delete a comment from a feed post and decrement post commentsCount
     */
    suspend fun deletePostComment(postId: String, commentId: String): Result<Unit> {
        val postRef = db.collection("posts").document(postId)
        val commentRef = postRef.collection("comments").document(commentId)

        return try {
            commentRef.delete().await()
            postRef.update(
                mapOf(
                    "commentsCount" to FieldValue.increment(-1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            AppLogger.i("Deleted comment $commentId from post $postId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(handleFirestoreError(e, "DELETE", commentRef.path))
        }
    }
}
