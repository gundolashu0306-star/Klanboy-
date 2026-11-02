package com.example

import com.example.data.model.FeedComment
import com.example.data.model.FeedPost
import com.example.data.model.SouthAfricanCampus
import com.example.ui.screens.feed.FeedUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedUnitTest {

    @Test
    fun `test FeedPost model creation with text and media attachment`() {
        val post = FeedPost(
            id = "feed_1001",
            authorId = "student_sbu",
            authorName = "Sibusiso Khumalo",
            authorPhotoUrl = "https://example.com/avatar.jpg",
            authorCampus = "University of Johannesburg",
            isAuthorVerified = true,
            content = "NSFAS allowance disbursement dates published on student portal!",
            imageUrl = "https://example.com/nsfas_schedule.png",
            likesCount = 5L,
            likedBy = listOf("student_1", "student_2", "student_3", "student_4", "student_5"),
            commentsCount = 2L
        )

        assertEquals("feed_1001", post.id)
        assertEquals("student_sbu", post.authorId)
        assertEquals("Sibusiso Khumalo", post.authorName)
        assertEquals("https://example.com/avatar.jpg", post.authorPhotoUrl)
        assertTrue(post.isAuthorVerified)
        assertEquals("https://example.com/nsfas_schedule.png", post.imageUrl)
        assertEquals(5L, post.likesCount)
        assertEquals(2L, post.commentsCount)
        assertTrue(post.likedBy.contains("student_1"))
        assertFalse(post.likedBy.contains("student_unliked"))
    }

    @Test
    fun `test FeedPost like toggling simulation`() {
        val originalPost = FeedPost(
            id = "feed_1002",
            authorId = "author_1",
            authorName = "Lerato Molefe",
            content = "Study group meeting at Wits Wartenweiler Library Level 3 at 4PM.",
            likesCount = 0L,
            likedBy = emptyList()
        )

        // Student A likes post
        val likedPost = originalPost.copy(
            likesCount = originalPost.likesCount + 1,
            likedBy = originalPost.likedBy + "student_a"
        )
        assertEquals(1L, likedPost.likesCount)
        assertTrue(likedPost.likedBy.contains("student_a"))

        // Student A unlikes post
        val unlikedPost = likedPost.copy(
            likesCount = (likedPost.likesCount - 1).coerceAtLeast(0),
            likedBy = likedPost.likedBy - "student_a"
        )
        assertEquals(0L, unlikedPost.likesCount)
        assertFalse(unlikedPost.likedBy.contains("student_a"))
    }

    @Test
    fun `test FeedComment creation and association with parent post`() {
        val comment = FeedComment(
            id = "comment_501",
            postId = "feed_1002",
            authorId = "student_b",
            authorName = "Zanele Mkhize",
            authorPhotoUrl = "https://example.com/zanele.png",
            isAuthorVerified = true,
            content = "Count me in! Are we working on calculus or linear algebra?"
        )

        assertEquals("comment_501", comment.id)
        assertEquals("feed_1002", comment.postId)
        assertEquals("student_b", comment.authorId)
        assertEquals("Zanele Mkhize", comment.authorName)
        assertTrue(comment.isAuthorVerified)
        assertEquals("Count me in! Are we working on calculus or linear algebra?", comment.content)
    }

    @Test
    fun `test FeedUiState campus filtering filters posts correctly`() {
        val ujPost = FeedPost(
            id = "post_uj",
            authorId = "user_uj",
            authorName = "UJ Student",
            authorCampus = "UJ",
            content = "UJ Kingsway campus shuttle running on normal schedule."
        )

        val witsPost = FeedPost(
            id = "post_wits",
            authorId = "user_wits",
            authorName = "Wits Student",
            authorCampus = "Wits",
            content = "Wits Great Hall career fair today!"
        )

        val allPosts = listOf(ujPost, witsPost)

        // When selectedCampusFilter is null ("All Mzansi"), all posts are displayed
        val allState = FeedUiState(
            posts = allPosts,
            selectedCampusFilter = null
        )
        assertEquals(2, allState.displayedPosts.size)

        // When UJ is selected, only UJ posts are displayed
        val ujState = FeedUiState(
            posts = allPosts,
            selectedCampusFilter = SouthAfricanCampus.UJ
        )
        assertEquals(1, ujState.displayedPosts.size)
        assertEquals("post_uj", ujState.displayedPosts.first().id)

        // When Wits is selected, only Wits posts are displayed
        val witsState = FeedUiState(
            posts = allPosts,
            selectedCampusFilter = SouthAfricanCampus.WITS
        )
        assertEquals(1, witsState.displayedPosts.size)
        assertEquals("post_wits", witsState.displayedPosts.first().id)
    }
}
