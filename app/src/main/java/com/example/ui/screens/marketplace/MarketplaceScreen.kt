package com.example.ui.screens.marketplace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView

@Composable
fun MarketplaceScreen(
    onNavigateToReadiness: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("marketplace_screen")
    ) {
        Text(
            text = "Student Marketplace",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Peer-to-peer campus commerce for textbooks, tech & essentials",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        EmptyStateView(
            title = "Marketplace Foundation Ready",
            description = "Data models (PlugPost), Firebase Storage contracts, and Firestore schemas are configured. Listings will synchronize live once backend is provisioned.",
            icon = Icons.Default.ShoppingBag,
            actionButtonText = "View Diagnostics",
            onActionClick = onNavigateToReadiness
        )
    }
}
