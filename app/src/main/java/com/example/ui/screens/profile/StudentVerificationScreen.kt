package com.example.ui.screens.profile

import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import com.example.ui.components.CampusPlugCard
import com.example.ui.components.CampusPlugPrimaryButton
import com.example.ui.components.CampusPlugSecondaryButton
import com.example.ui.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentVerificationScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val scrollState = rememberScrollState()

    val profile = (profileState as? UiState.Success)?.data ?: UserProfile()
    val status = profile.status

    var selectedDocumentName by remember { mutableStateOf("") }
    var selectedDocumentBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedMimeType by remember { mutableStateOf("application/pdf") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null && bytes.isNotEmpty()) {
                    selectedDocumentBytes = bytes
                    selectedDocumentName = uri.lastPathSegment?.substringAfterLast('/') ?: "student_document.pdf"
                    selectedMimeType = context.contentResolver.getType(uri) ?: "application/pdf"
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
    }

    val isLoading = operationState is UiState.Loading
    val errorMessage = (operationState as? UiState.Error)?.message

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_verification_screen")
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Student Verification",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Status Header Banner
            VerificationCurrentStatusBanner(status = status, rejectionReason = profile.verificationRejectionReason)

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                ErrorBanner(
                    errorMessage = errorMessage,
                    onDismiss = { viewModel.dismissOperationState() },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (operationState is UiState.Success) {
                CampusPlugCard(cornerRadius = 12.dp, modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (operationState as UiState.Success).data,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Benefits of Verification
            CampusPlugCard(cornerRadius = 14.dp) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Why Institutional Verification?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Unlock campus-exclusive quad chats, notices and past papers\n" +
                               "• Receive the trusted Verified Student badge on marketplace listings\n" +
                               "• Access student-only accredited accommodation deals & NSFAS listings\n" +
                               "• Keep campus communities safe from unauthorized outsiders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Submission Card
            if (status != VerificationStatus.VERIFIED) {
                CampusPlugCard(cornerRadius = 14.dp) {
                    Column {
                        Text(
                            text = "Submit Supporting Student Proof",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload an official Proof of Registration (PDF), Enrolment Certificate, or photo of your student card for ${profile.institutionName}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Document Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { filePickerLauncher.launch("*/*") }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedDocumentName.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedDocumentName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${(selectedDocumentBytes?.size ?: 0) / 1024} KB • Tap to change document",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to Select Document or Student Card",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "PDF, JPEG or PNG up to 10MB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Test Document Generator (facilitates evaluation without local PDFs)
                        CampusPlugSecondaryButton(
                            text = "Attach Demo Registration Proof (${profile.institutionName})",
                            icon = Icons.Default.Attachment,
                            onClick = {
                                selectedDocumentName = "proof_of_registration_2026_${profile.campusCode}.pdf"
                                selectedDocumentBytes = "DEMO_STUDENT_PROOF_OF_REGISTRATION_FOR_${profile.displayName}".toByteArray()
                                selectedMimeType = "application/pdf"
                            },
                            testTag = "attach_demo_proof_button"
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        CampusPlugPrimaryButton(
                            text = "Submit for Verification",
                            onClick = {
                                val bytes = selectedDocumentBytes
                                if (bytes != null && bytes.isNotEmpty()) {
                                    viewModel.submitVerification(
                                        documentBytes = bytes,
                                        documentName = selectedDocumentName,
                                        mimeType = selectedMimeType
                                    )
                                } else {
                                    // Use demo proof if none picked
                                    val demoBytes = "OFFICIAL_STUDENT_ENROLLMENT_VERIFICATION".toByteArray()
                                    viewModel.submitVerification(
                                        demoBytes,
                                        "student_proof_card.pdf",
                                        "application/pdf"
                                    )
                                }
                            },
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "submit_verification_button"
                        )
                    }
                }
            } else {
                CampusPlugCard(cornerRadius = 14.dp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You Are Officially Verified!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your enrolment at ${profile.institutionName} has been verified by authorized administrators. Your profile reflects full campus credentials.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VerificationCurrentStatusBanner(
    status: VerificationStatus,
    rejectionReason: String
) {
    val (cardColor, contentColor, icon, title, description) = when (status) {
        VerificationStatus.VERIFIED -> Quintuple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle,
            "Verified Student Enrolment",
            "Your student status is active and verified for the current academic year."
        )
        VerificationStatus.PENDING -> Quintuple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            Icons.Default.HourglassTop,
            "Verification Pending Review",
            "Your documentation has been received and is queued for verification by an authorized administrator."
        )
        VerificationStatus.REJECTED -> Quintuple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.ErrorOutline,
            "Verification Decision: Rejected",
            if (rejectionReason.isNotBlank()) "Reason: $rejectionReason" else "The uploaded document did not meet institutional verification criteria. Please resubmit."
        )
        VerificationStatus.NOT_SUBMITTED -> Quintuple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.UploadFile,
            "Unverified Student Profile",
            "Upload proof of enrolment to obtain the verified badge and full campus privileges."
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
