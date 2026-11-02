package com.example.ui.screens.campuses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Community
import com.example.data.model.SouthAfricanCampus
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.CampusTagBadge
import com.example.ui.components.EmptyStateView

@Composable
fun CampusesScreen(
    viewModel: CommunityViewModel,
    onNavigateToCommunity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCreateDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage, uiState.errorMessage) {
        val message = uiState.userMessage ?: uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    val student = uiState.currentStudent
    val currentUserId = student?.id

    val defaultCampus = SouthAfricanCampus.entries.find {
        it.code.equals(student?.campusCode, ignoreCase = true)
    } ?: SouthAfricanCampus.UJ

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreateDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_create_community")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Community")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Community",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("campuses_screen")
        ) {
            // Campus Header Banner
            CampusHeaderSection(
                campusName = student?.campusName ?: "South Africa",
                institutionName = student?.institutionName ?: "Connected Universities",
                activeCommunitiesCount = uiState.allCommunities.size
            )

            // Tabs: My Campus / All Campuses / Joined
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("community_tab_row")
            ) {
                Tab(
                    selected = uiState.selectedTab == CommunityTabFilter.MY_CAMPUS,
                    onClick = { viewModel.setTabFilter(CommunityTabFilter.MY_CAMPUS) },
                    text = { Text("My Campus", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_my_campus")
                )
                Tab(
                    selected = uiState.selectedTab == CommunityTabFilter.ALL,
                    onClick = { viewModel.setTabFilter(CommunityTabFilter.ALL) },
                    text = { Text("All SA", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_all_campuses")
                )
                Tab(
                    selected = uiState.selectedTab == CommunityTabFilter.JOINED,
                    onClick = { viewModel.setTabFilter(CommunityTabFilter.JOINED) },
                    text = { Text("Joined", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_joined_communities")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search societies, clubs, faculty circles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_communities_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(COMMUNITY_CATEGORIES) { category ->
                    val isSelected = uiState.selectedCategory.equals(category, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_${category.replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Community List or Loading
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loading_communities"))
                }
            } else if (uiState.filteredCommunities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = when (uiState.selectedTab) {
                            CommunityTabFilter.JOINED -> "No Communities Joined"
                            CommunityTabFilter.MY_CAMPUS -> "No Campus Circles Yet"
                            CommunityTabFilter.ALL -> "No Communities Found"
                        },
                        description = when (uiState.selectedTab) {
                            CommunityTabFilter.JOINED -> "Explore campus societies and join discussion spaces to connect with peers."
                            else -> "Be the first student to launch a community for your campus!"
                        },
                        icon = Icons.Default.Groups
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("communities_lazy_column"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredCommunities, key = { it.id }) { community ->
                        CommunityCardItem(
                            community = community,
                            currentUserId = currentUserId,
                            onJoin = { viewModel.joinCommunity(community) },
                            onLeave = { viewModel.leaveCommunity(community) },
                            onClick = { onNavigateToCommunity(community.id) }
                        )
                    }
                }
            }
        }
    }

    if (isCreateDialogOpen) {
        CreateCommunityDialog(
            initialCampus = defaultCampus,
            onDismiss = { isCreateDialogOpen = false },
            onSubmit = { name, category, description, campus ->
                viewModel.createCommunity(name, category, description, campus) { success, _ ->
                    if (success) {
                        isCreateDialogOpen = false
                    }
                }
            },
            isSubmitting = uiState.isCreating
        )
    }
}

@Composable
private fun CampusHeaderSection(
    campusName: String,
    institutionName: String,
    activeCommunitiesCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Campus Communities",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$institutionName • $campusName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "$activeCommunitiesCount Active",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CommunityCardItem(
    community: Community,
    currentUserId: String?,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onClick: () -> Unit
) {
    val isMember = community.isMember(currentUserId)

    CampusPlugCard(
        cornerRadius = 14.dp,
        onClick = onClick,
        testTag = "community_card_${community.id}"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CampusTagBadge(campusName = community.campusName)
                Text(
                    text = community.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = community.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (community.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${community.memberCount} members",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isMember) {
                    OutlinedButton(
                        onClick = onLeave,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("leave_btn_${community.id}")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Joined", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("join_btn_${community.id}")
                    ) {
                        Text("Join", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
