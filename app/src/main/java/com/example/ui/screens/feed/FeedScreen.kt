package com.example.ui.screens.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FeedComment
import com.example.data.model.FeedPost
import com.example.data.model.SouthAfricanCampus
import com.example.ui.components.CampusPlugPrimaryButton
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.CampusTagBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorBanner
import com.example.ui.components.StatusPill
import com.example.ui.components.VerifiedStudentBadge
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                null
            }
            viewModel.onImageSelected(uri, bytes)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("student_feed_list")
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Error Banner
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    uiState.errorMessage?.let {
                        ErrorBanner(
                            errorMessage = it,
                            onDismiss = { viewModel.clearFeedback() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Campus Filter Chips
                CampusFilterRow(
                    selectedCampus = uiState.selectedCampusFilter,
                    onSelectCampus = { viewModel.onCampusFilterSelected(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Post Composer Card
                PostComposerCard(
                    draftText = uiState.postDraft,
                    onDraftChange = { viewModel.onPostDraftChanged(it) },
                    selectedImageUri = uiState.selectedImageUri,
                    onAttachPhotoClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemovePhotoClick = { viewModel.clearSelectedImage() },
                    onPublishClick = { viewModel.publishPost() },
                    isPublishing = uiState.isPublishing,
                    studentName = uiState.currentStudent?.displayName ?: "Student"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedCampusFilter == null) "Mzansi Campus Pulse" else "${uiState.selectedCampusFilter?.shortName} Feed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.displayedPosts.size} posts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            } else if (uiState.displayedPosts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Forum,
                        title = "No Campus Posts Yet",
                        description = if (uiState.selectedCampusFilter != null) {
                            "Be the first to share an update, past paper, or notice for ${uiState.selectedCampusFilter?.institutionName}!"
                        } else {
                            "The feed is quiet. Share an announcement, ask a question, or attach a campus notice above!"
                        },
                        actionButtonText = "Share a Plug",
                        onActionClick = { viewModel.onPostDraftChanged("Hello Campus Plug SA! ") }
                    )
                }
            } else {
                items(
                    items = uiState.displayedPosts,
                    key = { it.id }
                ) { post ->
                    val isLiked = uiState.currentStudent?.let { post.likedBy.contains(it.id) } == true
                    val isOwner = uiState.currentStudent?.id == post.authorId || uiState.currentStudent?.isAdmin == true

                    FeedPostCard(
                        post = post,
                        isLiked = isLiked,
                        isOwner = isOwner,
                        onLikeClick = { viewModel.toggleLike(post) },
                        onCommentClick = { viewModel.openComments(post) },
                        onDeleteClick = { viewModel.deletePost(post.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Comments Bottom Sheet
        uiState.commentSheetState?.let { sheetState ->
            PostCommentsBottomSheet(
                sheetState = sheetState,
                currentUserId = uiState.currentStudent?.id.orEmpty(),
                isAdmin = uiState.currentStudent?.isAdmin == true,
                onDismiss = { viewModel.closeComments() },
                onDraftChange = { viewModel.onCommentDraftChanged(it) },
                onSubmitComment = { viewModel.submitComment() },
                onDeleteComment = { commentId ->
                    viewModel.deleteComment(sheetState.post.id, commentId)
                }
            )
        }
    }
}

@Composable
private fun CampusFilterRow(
    selectedCampus: SouthAfricanCampus?,
    onSelectCampus: (SouthAfricanCampus?) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Campuses" chip
        val isAllSelected = selectedCampus == null
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isAllSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { onSelectCampus(null) }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("campus_chip_all")
        ) {
            Text(
                text = "All Mzansi",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        listOf(
            SouthAfricanCampus.UJ,
            SouthAfricanCampus.WITS,
            SouthAfricanCampus.UP,
            SouthAfricanCampus.UCT,
            SouthAfricanCampus.STELLENBOSCH,
            SouthAfricanCampus.TUT,
            SouthAfricanCampus.CPUT,
            SouthAfricanCampus.UKZN,
            SouthAfricanCampus.TVET_COLLEGES
        ).forEach { campus ->
            val isSelected = campus == selectedCampus
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelectCampus(campus) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("campus_chip_${campus.code}")
            ) {
                Text(
                    text = campus.shortName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostComposerCard(
    draftText: String,
    onDraftChange: (String) -> Unit,
    selectedImageUri: Uri?,
    onAttachPhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    onPublishClick: () -> Unit,
    isPublishing: Boolean,
    studentName: String
) {
    CampusPlugCard(
        cornerRadius = 16.dp,
        elevation = 2.dp,
        testTag = "feed_post_composer"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "What's happening on campus, $studentName?",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draftText,
                onValueChange = onDraftChange,
                placeholder = {
                    Text(
                        "Share an announcement, ask a question, NSFAS update, or campus notice...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("feed_post_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Image Preview if selected
            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected post attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = onRemovePhotoClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onAttachPhotoClick,
                    enabled = !isPublishing,
                    modifier = Modifier.testTag("attach_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedImageUri != null) "Change Photo" else "Attach Photo",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                CampusPlugPrimaryButton(
                    text = "Post Plug",
                    onClick = onPublishClick,
                    isLoading = isPublishing,
                    enabled = (draftText.isNotBlank() || selectedImageUri != null) && !isPublishing,
                    testTag = "publish_post_button",
                    modifier = Modifier
                        .width(130.dp)
                        .height(44.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedPostCard(
    post: FeedPost,
    isLiked: Boolean,
    isOwner: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    CampusPlugCard(
        cornerRadius = 16.dp,
        elevation = 1.dp,
        modifier = modifier.testTag("feed_card_${post.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Post Header: Avatar, Name, Campus, Time, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Profile picture
                    if (post.authorPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(post.authorPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "${post.authorName}'s profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.authorName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (post.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedStudentBadge()
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            if (post.authorCampus.isNotBlank()) {
                                CampusTagBadge(campusName = post.authorCampus)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = formatTimeAgo(post.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isOwner) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Post options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Content Text
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Attached Image
            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post attachment image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(6.dp))

            // Interaction Bar: Like & Comment buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike post" else "Like post",
                        tint = if (isLiked) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (post.likesCount > 0) "${post.likesCount}" else "Like",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isLiked) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCommentClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "View comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (post.commentsCount > 0) "${post.commentsCount} comments" else "Comment",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostCommentsBottomSheet(
    sheetState: CommentSheetState,
    currentUserId: String,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onDeleteComment: (String) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("comments_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Discussion & Comments",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Plug by ${sheetState.post.authorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close comments")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Comments List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(300.dp)
            ) {
                if (sheetState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else if (sheetState.comments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No comments yet. Start the conversation!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = sheetState.comments,
                            key = { it.id }
                        ) { comment ->
                            val canDelete = comment.authorId == currentUserId || isAdmin
                            CommentItem(
                                comment = comment,
                                canDelete = canDelete,
                                onDelete = { onDeleteComment(comment.id) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = sheetState.draftComment,
                    onValueChange = onDraftChange,
                    placeholder = { Text("Write a comment...", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSubmitComment,
                    enabled = sheetState.draftComment.isNotBlank() && !sheetState.isPosting,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (sheetState.draftComment.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("submit_comment_button")
                ) {
                    if (sheetState.isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send comment",
                            tint = if (sheetState.draftComment.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: FeedComment,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp)
            .testTag("comment_item_${comment.id}")
    ) {
        if (comment.authorPhotoUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comment.authorPhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.authorName.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (comment.isAuthorVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedStudentBadge()
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (canDelete) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete comment",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    val diffMillis = System.currentTimeMillis() - timestamp.toDate().time
    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(timestamp.toDate())
        }
    }
}
