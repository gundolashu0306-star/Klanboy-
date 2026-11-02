package com.example.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.data.model.UserProfile
import com.example.ui.components.CampusPlugPrimaryButton
import com.example.ui.components.CampusPlugSecondaryButton
import com.example.ui.components.ErrorBanner

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val profileState by viewModel.profileState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val scrollState = rememberScrollState()

    val initialProfile = (profileState as? UiState.Success)?.data ?: UserProfile()

    var displayName by remember(initialProfile) { mutableStateOf(initialProfile.displayName) }
    var course by remember(initialProfile) { mutableStateOf(initialProfile.effectiveCourse) }
    var yearOfStudy by remember(initialProfile) { mutableIntStateOf(initialProfile.yearOfStudy) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var bio by remember(initialProfile) { mutableStateOf(initialProfile.bio) }
    var phoneNumber by remember(initialProfile) { mutableStateOf(initialProfile.phoneNumber) }
    var campusName by remember(initialProfile) { mutableStateOf(initialProfile.campusName) }

    val isLoading = operationState is UiState.Loading
    val errorMessage = (operationState as? UiState.Error)?.message

    if (operationState is UiState.Success) {
        viewModel.dismissOperationState()
        onNavigateBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("edit_profile_screen")
    ) {
        Text(
            text = "Edit Student Profile",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Update your verified details and public student bio",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            ErrorBanner(
                errorMessage = errorMessage,
                onDismiss = { viewModel.dismissOperationState() },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Full Name
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display / Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_display_name_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campus Location / Branch
        OutlinedTextField(
            value = campusName,
            onValueChange = { campusName = it },
            label = { Text("Campus Location / Branch") },
            placeholder = { Text("e.g. Auckland Park Kingsway Campus") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_campus_branch_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Course / Qualification
        OutlinedTextField(
            value = course,
            onValueChange = { course = it },
            label = { Text("Course or Qualification") },
            placeholder = { Text("e.g. BCom Accounting Sciences") },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_course_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Year of study
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = "Year $yearOfStudy",
                onValueChange = {},
                readOnly = true,
                label = { Text("Year of Study") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select",
                        modifier = Modifier.clickable { yearDropdownExpanded = true }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { yearDropdownExpanded = true }
                    .testTag("edit_year_dropdown")
            )

            DropdownMenu(
                expanded = yearDropdownExpanded,
                onDismissRequest = { yearDropdownExpanded = false }
            ) {
                (1..6).forEach { yr ->
                    DropdownMenuItem(
                        text = { Text("Year $yr") },
                        onClick = {
                            yearOfStudy = yr
                            yearDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phone Number
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("WhatsApp / Phone Number") },
            placeholder = { Text("e.g. 082 123 4567") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_phone_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Short Biography
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Student Biography & Campus Interests") },
            placeholder = { Text("Introduce yourself, your societies, sports, or study focus...") },
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_bio_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        CampusPlugPrimaryButton(
            text = "Save Changes",
            onClick = {
                focusManager.clearFocus()
                viewModel.updateProfile(
                    displayName = displayName,
                    course = course,
                    yearOfStudy = yearOfStudy,
                    bio = bio,
                    phoneNumber = phoneNumber,
                    campusName = campusName
                )
            },
            isLoading = isLoading,
            enabled = !isLoading,
            testTag = "save_profile_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        CampusPlugSecondaryButton(
            text = "Cancel",
            onClick = onNavigateBack,
            enabled = !isLoading,
            testTag = "cancel_edit_profile_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
