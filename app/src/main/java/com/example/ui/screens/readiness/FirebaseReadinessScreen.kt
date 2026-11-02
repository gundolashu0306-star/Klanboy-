package com.example.ui.screens.readiness

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.firebase.FirebaseConfig
import com.example.data.firebase.FirebaseReadinessReport
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.CampusPlugPrimaryButton
import com.example.ui.components.CampusPlugSecondaryButton
import com.example.ui.components.StatusPill

@Composable
fun FirebaseReadinessScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var report by remember { mutableStateOf(FirebaseConfig.evaluateReadiness(context)) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("readiness_screen")
    ) {
        Text(
            text = "System Readiness & Architecture",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Phase 1 Foundation Verification & Backend Infrastructure Status",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Overall Status Banner
        OverallStatusCard(report)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Service Integrations Readiness",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Service 1: Firebase Core
        ServiceStatusRow(
            title = "Firebase App & Google Services",
            subtitle = if (report.isFirebaseInitialized) "Core initialized via google-services.json" else "Pending project binding / terms confirmation",
            isConfigured = report.isFirebaseInitialized,
            icon = Icons.Default.CloudQueue
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Service 2: Authentication
        ServiceStatusRow(
            title = "Firebase Authentication (Google Sign-In)",
            subtitle = if (report.isGoogleSignInReady) "Web Client ID: ${report.webClientId.take(16)}..." else "Pending Web Client ID provisioning in Google Cloud",
            isConfigured = report.isGoogleSignInReady,
            icon = Icons.Default.VpnKey
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Service 3: Cloud Firestore
        ServiceStatusRow(
            title = "Cloud Firestore Database",
            subtitle = "Custom Database ID: ${report.firestoreDatabaseId} (Rule models prepared)",
            isConfigured = report.isFirestoreReady,
            icon = Icons.Default.DataObject
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Service 4: Cloud Storage
        ServiceStatusRow(
            title = "Firebase Cloud Storage",
            subtitle = "Contract configured for student IDs, res photos & marketplace",
            isConfigured = report.isStorageReady,
            icon = Icons.Default.FolderZip
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Package & Signing Fingerprint Diagnostic Card
        BuildSigningCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Outstanding Actions
        if (report.missingRequirements.isNotEmpty()) {
            MissingRequirementsCard(report.missingRequirements)
            Spacer(modifier = Modifier.height(16.dp))
        }

        CampusPlugSecondaryButton(
            text = "Re-evaluate System Readiness",
            icon = Icons.Default.Refresh,
            onClick = {
                report = FirebaseConfig.evaluateReadiness(context)
            },
            testTag = "refresh_readiness_button"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OverallStatusCard(report: FirebaseReadinessReport) {
    CampusPlugCard(
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (report.isFullyReady) MaterialTheme.colorScheme.primary
                                else Color(0xFFF59E0B)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phase 1 Foundation Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                StatusPill(
                    label = if (report.isFullyReady) "Connected" else "Foundation Ready",
                    containerColor = if (report.isFullyReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (report.isFullyReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (report.isFullyReady) {
                    "All backend cloud services are verified and operational. System is primed for Phase 2."
                } else {
                    "Architecture, navigation, typography, reusable components, error logging, and repository layers are fully established and compile cleanly. Backend is ready for live Firebase provisioning."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ServiceStatusRow(
    title: String,
    subtitle: String,
    isConfigured: Boolean,
    icon: ImageVector
) {
    CampusPlugCard(cornerRadius = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Pending,
                contentDescription = if (isConfigured) "Configured" else "Pending",
                tint = if (isConfigured) MaterialTheme.colorScheme.primary else Color(0xFFF59E0B),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun BuildSigningCard() {
    CampusPlugCard(cornerRadius = 12.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Release & Environment Telemetry",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            DiagnosticRow("Package Name", BuildConfig.APPLICATION_ID)
            DiagnosticRow("Build Version", "${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})")
            DiagnosticRow("Architecture", "MVVM / Clean / Jetpack Compose")
            DiagnosticRow("Signing Keystore", "debug.keystore (Materialized)")
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
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
private fun MissingRequirementsCard(missing: List<String>) {
    CampusPlugCard(
        cornerRadius = 12.dp
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Outstanding Configuration Steps",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            missing.forEachIndexed { index, req ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "${index + 1}. ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = req,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
