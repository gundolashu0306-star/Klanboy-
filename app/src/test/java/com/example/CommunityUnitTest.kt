package com.example

import com.example.data.model.Community
import com.example.data.model.CommunityPost
import com.example.data.model.SouthAfricanCampus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityUnitTest {

    @Test
    fun `test Community model creation and creator is member invariant`() {
        val community = Community(
            id = "wits_dev_circle",
            name = "Wits Developer Circle",
            description = "Collaborative coding and hackathon prep for Wits students",
            category = "Academic & Tech",
            campusCode = "wits",
            institutionName = "University of the Witwatersrand",
            campusName = "Braamfontein",
            creatorId = "creator_uid_101",
            creatorName = "Kagiso Rabada",
            memberCount = 1L,
            memberUids = listOf("creator_uid_101")
        )

        assertEquals("wits_dev_circle", community.id)
        assertEquals("Wits Developer Circle", community.name)
        assertEquals("Academic & Tech", community.category)
        assertEquals(1L, community.memberCount)

        // Creator is always recognized as a member
        assertTrue(community.isMember("creator_uid_101"))
        assertFalse(community.isMember("random_visitor_uid"))
        assertFalse(community.isMember(null))
    }

    @Test
    fun `test Community member list membership resolution`() {
        val community = Community(
            id = "uj_football",
            name = "UJ Campus Football Club",
            creatorId = "coach_uid",
            memberUids = listOf("coach_uid", "student_player_1", "student_player_2"),
            memberCount = 3L
        )

        assertTrue(community.isMember("coach_uid"))
        assertTrue(community.isMember("student_player_1"))
        assertTrue(community.isMember("student_player_2"))
        assertFalse(community.isMember("non_member_uid"))
    }

    @Test
    fun `test CommunityPost creation and like state tracking`() {
        val post = CommunityPost(
            id = "post_101",
            communityId = "wits_dev_circle",
            authorId = "author_uid",
            authorName = "Nandi Madida",
            isAuthorVerified = true,
            title = "Intro to Kotlin 2.2 workshop",
            content = "Join us this Friday in the lab for hands-on Compose multiplatform and backend integration.",
            likesCount = 2L,
            likedBy = listOf("peer_student_1", "peer_student_2")
        )

        assertEquals("post_101", post.id)
        assertEquals("wits_dev_circle", post.communityId)
        assertTrue(post.isAuthorVerified)
        assertEquals(2L, post.likesCount)

        // Like checks
        assertTrue(post.isLikedBy("peer_student_1"))
        assertTrue(post.isLikedBy("peer_student_2"))
        assertFalse(post.isLikedBy("author_uid"))
        assertFalse(post.isLikedBy(null))
    }

    @Test
    fun `test campus matching for community discovery`() {
        val ujCampus = SouthAfricanCampus.UJ
        val witsCampus = SouthAfricanCampus.WITS

        val ujCommunity = Community(
            id = "uj_robotics",
            name = "UJ Robotics Society",
            campusCode = ujCampus.code,
            institutionName = ujCampus.institutionName
        )

        val witsCommunity = Community(
            id = "wits_law",
            name = "Wits Law Students Association",
            campusCode = witsCampus.code,
            institutionName = witsCampus.institutionName
        )

        val allCommunities = listOf(ujCommunity, witsCommunity)

        // Filter for UJ campus
        val ujFiltered = allCommunities.filter { it.campusCode.equals(ujCampus.code, ignoreCase = true) }
        assertEquals(1, ujFiltered.size)
        assertEquals("uj_robotics", ujFiltered.first().id)

        // Filter for Wits campus
        val witsFiltered = allCommunities.filter { it.campusCode.equals(witsCampus.code, ignoreCase = true) }
        assertEquals(1, witsFiltered.size)
        assertEquals("wits_law", witsFiltered.first().id)
    }
}
