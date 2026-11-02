package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CommunityCategory
import com.example.data.model.SouthAfricanCampus
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.StatusPill
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.feed.FeedViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun DashboardScreen(
    feedViewModel: FeedViewModel,
    onNavigateToCategory: (CommunityCategory) -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDashboardTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        // Tab Row switching between Live Feed and Services Directory
        TabRow(
            selectedTabIndex = selectedDashboardTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedDashboardTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedDashboardTab == 0,
                onClick = { selectedDashboardTab = 0 },
                text = {
                    Text(
                        "Live Campus Feed",
                        fontWeight = if (selectedDashboardTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Live Campus Feed",
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("dashboard_tab_live_feed")
            )
            Tab(
                selected = selectedDashboardTab == 1,
                onClick = { selectedDashboardTab = 1 },
                text = {
                    Text(
                        "Campus Services",
                        fontWeight = if (selectedDashboardTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Campus Services Directory",
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("dashboard_tab_services")
            )
        }

        if (selectedDashboardTab == 0) {
            FeedScreen(
                viewModel = feedViewModel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CampusServicesDirectory(
                onNavigateToCategory = onNavigateToCategory,
                onNavigateToDiagnostics = onNavigateToDiagnostics,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CampusServicesDirectory(
    onNavigateToCategory: (CommunityCategory) -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCampus by remember { mutableStateOf(SouthAfricanCampus.UJ) }
    val scrollState = rememberScrollState()
    val campusScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("campus_services_directory")
    ) {
        // Welcome & Campus Switcher
        val user = Firebase.auth.currentUser
        val displayName = user?.displayName?.ifBlank { null } ?: user?.email?.substringBefore('@') ?: "Student"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mzansi Student Hub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Welcome, $displayName",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            StatusPill(
                label = "Active Student",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Campus Chips
        Text(
            text = "Your Active Campus",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(campusScrollState)
        ) {
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
                        .clickable { selectedCampus = campus }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = campus.shortName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Campus Hero Banner
        CampusPlugCard(cornerRadius = 16.dp, elevation = 2.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCampus.institutionName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selectedCampus.primaryCity}, ${selectedCampus.province}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusPill(
                        label = "Active Hub",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Ecosystem Modules",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Grid / List of Module Plugs
        EcosystemPlugCard(
            title = "Campus Pulse & Notices",
            description = "Timetables, NSFAS alerts, SRC announcements & student discussions.",
            icon = Icons.Default.Campaign,
            badge = "Essential",
            onClick = { onNavigateToCategory(CommunityCategory.CAMPUS_PULSE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EcosystemPlugCard(
            title = "Study Vault & Tutors",
            description = "Peer past papers, study groups, module notes and tutoring exchanges.",
            icon = Icons.Default.School,
            badge = "Academic",
            onClick = { onNavigateToCategory(CommunityCategory.ACADEMIC_RESOURCES) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EcosystemPlugCard(
            title = "Res & Off-Campus Pads",
            description = "Accredited student accommodation, roommate matches & landlord reviews.",
            icon = Icons.Default.Apartment,
            badge = "Housing",
            onClick = { onNavigateToCategory(CommunityCategory.ACCOMMODATION) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EcosystemPlugCard(
            title = "Student Marketplace",
            description = "Buy & sell secondhand textbooks, lab coats, electronics and dorm gear.",
            icon = Icons.Default.Storefront,
            badge = "Market",
            onClick = { onNavigateToCategory(CommunityCategory.STUDENT_MARKET) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EcosystemPlugCard(
            title = "Student Gigs & Bursaries",
            description = "Campus jobs, vac work, bursaries, internships & side hustles.",
            icon = Icons.Default.WorkOutline,
            badge = "Careers",
            onClick = { onNavigateToCategory(CommunityCategory.OPPORTUNITIES) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phase 1 Architecture verification callout
        CampusPlugCard(
            cornerRadius = 14.dp,
            onClick = onNavigateToDiagnostics,
            testTag = "phase1_architecture_card"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phase 1 Foundation Operational",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tap to view architecture layers, dependencies & backend readiness.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EcosystemPlugCard(
    title: String,
    description: String,
    icon: ImageVector,
    badge: String,
    onClick: () -> Unit
) {
    CampusPlugCard(
        cornerRadius = 14.dp,
        onClick = onClick,
        testTag = "plug_card_${title.replace(" ", "_").lowercase()}"
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    StatusPill(
                        label = badge,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
