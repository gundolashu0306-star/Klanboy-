package com.example.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.UiState
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.CampusPlugSecondaryButton
import com.example.ui.components.ErrorBanner
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.StatusPill

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onExploreHub: () -> Unit,
    onViewDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val isLoading = uiState is UiState.Loading
    val errorMessage = (uiState as? UiState.Error)?.message

    if (uiState is UiState.Success) {
        onAuthSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Brand Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = "Campus Plug Logo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CAMPUS PLUG",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            StatusPill(
                label = "SA",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The Verified Student Ecosystem",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Connect with students across 26 South African universities and TVET colleges. Access campus news, verified student housing, peer marketplaces, past papers, and student gigs.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Campus Pills preview
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 5,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("UJ", "Wits", "UCT", "UP", "Maties", "CPUT", "TUT", "UKZN", "TVET").forEach { campus ->
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = campus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (errorMessage != null) {
            ErrorBanner(
                errorMessage = errorMessage,
                onDismiss = { viewModel.dismissError() },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Google Sign-In Action
        GoogleSignInButton(
            onClick = {
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.signInWithGoogle(activity)
                }
            },
            isLoading = isLoading,
            enabled = !isLoading,
            testTag = "auth_google_sign_in_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bypass button for Phase 1 testing and exploration
        CampusPlugSecondaryButton(
            text = "Explore Foundation Hub",
            onClick = onExploreHub,
            enabled = !isLoading,
            testTag = "explore_hub_button"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // System Diagnostic link
        TextButton(
            onClick = onViewDiagnostics,
            modifier = Modifier.testTag("auth_view_diagnostics_button")
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "View Phase 1 System Diagnostics",
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security notice
        CampusPlugCard(cornerRadius = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Zero-trust verified campus safety. Only authorized students gain access to campus-specific plugs and housing reviews.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
