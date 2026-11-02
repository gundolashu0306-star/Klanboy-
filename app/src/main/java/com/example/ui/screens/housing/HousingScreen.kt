package com.example.ui.screens.housing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView

@Composable
fun HousingScreen(
    onNavigateToReadiness: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("housing_screen")
    ) {
        Text(
            text = "Res & Student Pads",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Accredited student accommodation, NSFAS accredited res & flatmates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        EmptyStateView(
            title = "Accommodation Hub Ready",
            description = "Campus-specific housing filters and Firebase Storage photo upload contracts are prepared. Accommodation listings activate upon Phase 2 rollout.",
            icon = Icons.Default.Apartment,
            actionButtonText = "View Diagnostics",
            onActionClick = onNavigateToReadiness
        )
    }
}
