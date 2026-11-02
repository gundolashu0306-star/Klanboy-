package com.example.ui.screens.profile

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.common.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.CampusPlugPrimaryButton
import com.example.ui.components.CampusPlugSecondaryButton
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorBanner
import com.example.ui.components.StatusPill

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val scrollState = rememberScrollState()

    var showAdminPanel by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: byteArrayOf()
                inputStream?.close()
                if (bytes.isNotEmpty()) {
                    val fileName = "profile_${System.currentTimeMillis()}.jpg"
                    viewModel.uploadProfilePhoto(bytes, fileName)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // Top Title & Edit Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Student Profile",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onNavigateToEditProfile,
                modifier = Modifier.testTag("profile_edit_icon_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (operationState is UiState.Error) {
            ErrorBanner(
                errorMessage = (operationState as UiState.Error).message,
                onDismiss = { viewModel.dismissOperationState() },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        } else if (operationState is UiState.Success) {
            CampusPlugCard(
                cornerRadius = 12.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = (operationState as UiState.Success).data,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        when (val state = profileState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Error -> {
                EmptyStateView(
                    title = "Unable to load student profile",
                    description = state.message,
                    actionButtonText = "Retry",
                    onActionClick = { viewModel.loadUserProfile() }
                )
            }

            is UiState.Empty -> {
                EmptyStateView(
                    title = "Student Profile Unavailable",
                    description = state.message,
                    actionButtonText = "Sign In",
                    onActionClick = onSignedOut
                )
            }

            is UiState.Success -> {
                val profile = state.data ?: UserProfile()
                ProfileContent(
                    profile = profile,
                    onPickPhoto = { photoPickerLauncher.launch("image/*") },
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToVerification = onNavigateToVerification,
                    onToggleAdmin = { showAdminPanel = !showAdminPanel }
                )

                // Authorised Admin Review Tool (Restricted Verification Decision Testing)
                if (showAdminPanel) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AdminVerificationDecisionPanel(
                        currentStatus = profile.status,
                        onApprove = { viewModel.performAdminVerificationDecision(VerificationStatus.VERIFIED) },
                        onReject = { reason -> viewModel.performAdminVerificationDecision(VerificationStatus.REJECTED, reason) },
                        onReset = { viewModel.performAdminVerificationDecision(VerificationStatus.PENDING) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Out Button
                CampusPlugSecondaryButton(
                    text = "Sign Out",
                    icon = Icons.Default.Logout,
                    onClick = { viewModel.signOut(context, onSignedOut) },
                    testTag = "profile_sign_out_button"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            is UiState.Idle -> {}
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    onPickPhoto: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onToggleAdmin: () -> Unit
) {
    // Top Hero Card with Avatar & Verification Badge
    CampusPlugCard(cornerRadius = 16.dp, elevation = 2.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onPickPhoto() },
                contentAlignment = Alignment.Center
            ) {
                if (profile.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                // Camera overlay badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Update photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.displayName.ifBlank { "Registered Student" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Verification Status Pill
            VerificationBadgePill(status = profile.status)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Verification Workflow Callout Card (if not yet verified)
    if (profile.status != VerificationStatus.VERIFIED) {
        CampusPlugCard(
            cornerRadius = 14.dp,
            onClick = onNavigateToVerification,
            testTag = "verification_action_card"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when (profile.status) {
                                VerificationStatus.PENDING -> Color(0xFFFEF3C7)
                                VerificationStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (profile.status) {
                            VerificationStatus.PENDING -> Icons.Default.HourglassTop
                            VerificationStatus.REJECTED -> Icons.Default.Cancel
                            else -> Icons.Default.Verified
                        },
                        contentDescription = null,
                        tint = when (profile.status) {
                            VerificationStatus.PENDING -> Color(0xFFD97706)
                            VerificationStatus.REJECTED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (profile.status) {
                            VerificationStatus.PENDING -> "Verification Under Review"
                            VerificationStatus.REJECTED -> "Verification Rejected (Tap to re-submit)"
                            else -> "Get Verified Student Badge"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = when (profile.status) {
                            VerificationStatus.PENDING -> "Your student registration proof is currently being audited by campus administrators."
                            VerificationStatus.REJECTED -> profile.verificationRejectionReason.ifBlank { "Documentation was invalid or illegible. Please submit official proof." }
                            else -> "Upload proof of registration or student card to unlock trusted campus access."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Academic Details Card
    CampusPlugCard(cornerRadius = 14.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Academic Institution & Course",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            ProfileInfoRow(label = "Institution", value = profile.institutionName)
            ProfileInfoRow(label = "Campus Branch", value = profile.campusName)
            ProfileInfoRow(label = "Qualification", value = profile.effectiveCourse.ifBlank { "Not specified" })
            ProfileInfoRow(label = "Year of Study", value = "Year ${profile.yearOfStudy}")
            if (profile.studentNumber.isNotBlank()) {
                ProfileInfoRow(label = "Student Number", value = profile.studentNumber)
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Student Biography Card
    CampusPlugCard(cornerRadius = 14.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Biography & Interests",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.bio.ifBlank { "No biography written yet. Tap edit profile to introduce yourself to your campus!" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (profile.bio.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Administrator Verification Testing & Reviewer Tool
    TextButton(
        onClick = onToggleAdmin,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Administrator Verification Reviewer (Dev/Audit)",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun VerificationBadgePill(status: VerificationStatus) {
    val (bgColor, textColor, icon) = when (status) {
        VerificationStatus.VERIFIED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle
        )
        VerificationStatus.PENDING -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            Icons.Default.HourglassTop
        )
        VerificationStatus.REJECTED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Cancel
        )
        VerificationStatus.NOT_SUBMITTED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Badge
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AdminVerificationDecisionPanel(
    currentStatus: VerificationStatus,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onReset: () -> Unit
) {
    CampusPlugCard(cornerRadius = 14.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authorized Administrator Review Panel",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Verification decisions are strictly restricted to authorized administrators. As required by Phase 2.1 specifications, students cannot self-verify.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampusPlugPrimaryButton(
                    text = "Approve (VERIFIED)",
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                )
                CampusPlugSecondaryButton(
                    text = "Reject",
                    onClick = { onReject("Document illegible or year of registration does not match.") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
