package com.example.data.model

/**
 * Pillars of Campus Plug SA student ecosystem.
 */
enum class CommunityCategory(
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    CAMPUS_PULSE("Campus Pulse", "News, strikes, exam timetables & alerts", "Campaign"),
    ACADEMIC_RESOURCES("Study Vault", "Past papers, summaries & tutor plugs", "School"),
    ACCOMMODATION("Res & Pads", "Accredited student housing & roommates", "Apartment"),
    STUDENT_MARKET("Student Market", "Textbooks, calculators, gadgets & food", "Storefront"),
    OPPORTUNITIES("Gigs & Bursaries", "Internships, NSFAS updates & side hustles", "WorkOutline"),
    COMMUNITY_CHATS("The Quad", "Vibe, events, clubs & society announcements", "Forum");
}
