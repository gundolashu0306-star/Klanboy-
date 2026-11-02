package com.example.ui.screens.feed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Result
import com.example.core.logging.AppLogger
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirestoreService
import com.example.data.firebase.StorageRepository
import com.example.data.model.FeedComment
import com.example.data.model.FeedPost
import com.example.data.model.SouthAfricanCampus
import com.example.data.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CommentSheetState(
    val post: FeedPost,
    val comments: List<FeedComment> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val draftComment: String = ""
)

data class FeedUiState(
    val currentStudent: UserProfile? = null,
    val posts: List<FeedPost> = emptyList(),
    val selectedCampusFilter: SouthAfricanCampus? = null, // null means "All Campuses"
    val postDraft: String = "",
    val selectedImageUri: Uri? = null,
    val selectedImageBytes: ByteArray? = null,
    val isPublishing: Boolean = false,
    val isLoading: Boolean = true,
    val commentSheetState: CommentSheetState? = null,
    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val displayedPosts: List<FeedPost>
        get() = if (selectedCampusFilter == null) {
            posts
        } else {
            posts.filter {
                it.authorCampus.equals(selectedCampusFilter.shortName, ignoreCase = true) ||
                it.authorCampus.equals(selectedCampusFilter.code, ignoreCase = true) ||
                it.authorCampus.equals(selectedCampusFilter.institutionName, ignoreCase = true)
            }
        }
}

class FeedViewModel(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var feedPostsJob: Job? = null
    private var activeCommentsJob: Job? = null

    init {
        observeCurrentUserProfile()
        observeFeedPosts()
    }

    private fun observeCurrentUserProfile() {
        authRepository.observeAuthState()
            .onEach { firebaseUser ->
                if (firebaseUser != null) {
                    firestoreService.observeUserProfile(firebaseUser.uid)
                        .onEach { profile ->
                            _uiState.update { it.copy(currentStudent = profile) }
                        }
                        .launchIn(viewModelScope)
                } else {
                    _uiState.update { it.copy(currentStudent = null) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFeedPosts() {
        feedPostsJob?.cancel()
        feedPostsJob = firestoreService.observeFeedPosts()
            .onEach { postList ->
                _uiState.update { it.copy(posts = postList, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onCampusFilterSelected(campus: SouthAfricanCampus?) {
        _uiState.update { it.copy(selectedCampusFilter = campus) }
    }

    fun onPostDraftChanged(text: String) {
        _uiState.update { it.copy(postDraft = text) }
    }

    fun onImageSelected(uri: Uri?, bytes: ByteArray?) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                selectedImageBytes = bytes
            )
        }
    }

    fun clearSelectedImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                selectedImageBytes = null
            )
        }
    }

    fun publishPost() {
        val state = _uiState.value
        val content = state.postDraft.trim()
        val student = state.currentStudent

        if (content.isBlank() && state.selectedImageBytes == null) {
            _uiState.update { it.copy(errorMessage = "Please enter some text or attach an image.") }
            return
        }

        if (student == null) {
            _uiState.update { it.copy(errorMessage = "You must be signed in to post to the campus feed.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, errorMessage = null) }

            var uploadedImageUrl = ""
            val imageBytes = state.selectedImageBytes
            if (imageBytes != null) {
                val fileName = "post_${UUID.randomUUID()}.jpg"
                when (val uploadResult = storageRepository.uploadFeedImage(student.id, fileName, imageBytes)) {
                    is Result.Success -> {
                        uploadedImageUrl = uploadResult.data
                    }
                    is Result.Error -> {
                        AppLogger.w("Image upload skipped or failed: ${uploadResult.error.userMessage}")
                    }
                    else -> Unit
                }
            }

            val authorCampus = student.campusName.ifBlank {
                student.institutionName.ifBlank { student.campusCode }
            }

            val newPost = FeedPost(
                authorId = student.id,
                authorName = student.displayName.ifBlank { "Student" },
                authorPhotoUrl = student.photoUrl,
                authorCampus = authorCampus,
                isAuthorVerified = student.isStudentVerified,
                content = content,
                imageUrl = uploadedImageUrl,
                likesCount = 0L,
                likedBy = emptyList(),
                commentsCount = 0L
            )

            when (val result = firestoreService.createFeedPost(newPost)) {
                is Result.Success -> {
                    AppLogger.i("Feed post published successfully: ${result.data}")
                    _uiState.update {
                        it.copy(
                            isPublishing = false,
                            postDraft = "",
                            selectedImageUri = null,
                            selectedImageBytes = null,
                            userMessage = "Post shared with campus!"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isPublishing = false,
                            errorMessage = result.error.userMessage
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (val result = firestoreService.deleteFeedPost(postId)) {
                is Result.Success -> {
                    _uiState.update {
                        // If comment sheet was open for this post, close it
                        val sheetState = if (it.commentSheetState?.post?.id == postId) null else it.commentSheetState
                        it.copy(
                            commentSheetState = sheetState,
                            userMessage = "Post deleted."
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.error.userMessage) }
                }
                else -> Unit
            }
        }
    }

    fun toggleLike(post: FeedPost) {
        val student = _uiState.value.currentStudent ?: return
        val isCurrentlyLiked = post.likedBy.contains(student.id)

        viewModelScope.launch {
            firestoreService.toggleFeedPostLike(post.id, student.id, isCurrentlyLiked)
        }
    }

    fun openComments(post: FeedPost) {
        activeCommentsJob?.cancel()
        _uiState.update {
            it.copy(
                commentSheetState = CommentSheetState(
                    post = post,
                    isLoading = true
                )
            )
        }

        activeCommentsJob = firestoreService.observePostComments(post.id)
            .onEach { commentList ->
                _uiState.update { current ->
                    current.commentSheetState?.let { sheet ->
                        if (sheet.post.id == post.id) {
                            current.copy(
                                commentSheetState = sheet.copy(
                                    comments = commentList,
                                    isLoading = false
                                )
                            )
                        } else current
                    } ?: current
                }
            }
            .launchIn(viewModelScope)
    }

    fun closeComments() {
        activeCommentsJob?.cancel()
        _uiState.update { it.copy(commentSheetState = null) }
    }

    fun onCommentDraftChanged(text: String) {
        _uiState.update { current ->
            current.commentSheetState?.let { sheet ->
                current.copy(commentSheetState = sheet.copy(draftComment = text))
            } ?: current
        }
    }

    fun submitComment() {
        val sheet = _uiState.value.commentSheetState ?: return
        val content = sheet.draftComment.trim()
        val student = _uiState.value.currentStudent

        if (content.isBlank()) return
        if (student == null) {
            _uiState.update { it.copy(errorMessage = "You must be signed in to comment.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { current ->
                current.commentSheetState?.let {
                    current.copy(commentSheetState = it.copy(isPosting = true))
                } ?: current
            }

            val newComment = FeedComment(
                postId = sheet.post.id,
                authorId = student.id,
                authorName = student.displayName.ifBlank { "Student" },
                authorPhotoUrl = student.photoUrl,
                isAuthorVerified = student.isStudentVerified,
                content = content
            )

            when (val result = firestoreService.addPostComment(sheet.post.id, newComment)) {
                is Result.Success -> {
                    _uiState.update { current ->
                        current.commentSheetState?.let {
                            current.copy(
                                commentSheetState = it.copy(
                                    isPosting = false,
                                    draftComment = ""
                                )
                            )
                        } ?: current
                    }
                }
                is Result.Error -> {
                    _uiState.update { current ->
                        current.copy(
                            errorMessage = result.error.userMessage,
                            commentSheetState = current.commentSheetState?.copy(isPosting = false)
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            firestoreService.deletePostComment(postId, commentId)
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
