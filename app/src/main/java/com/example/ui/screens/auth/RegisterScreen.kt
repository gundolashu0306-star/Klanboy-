package com.example.ui.screens.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.data.model.SouthAfricanCampus
import com.example.ui.components.CampusPlugPrimaryButton
import com.example.ui.components.ErrorBanner

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var selectedCampus by remember { mutableStateOf(SouthAfricanCampus.UJ) }
    var campusDropdownExpanded by remember { mutableStateOf(false) }

    var campusBranchName by remember { mutableStateOf("Auckland Park Kingsway") }
    var studentNumber by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var yearOfStudy by remember { mutableIntStateOf(1) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    val isLoading = uiState is UiState.Loading
    val errorMessage = (uiState as? UiState.Error)?.message

    if (uiState is UiState.Success) {
        onRegisterSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("register_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Create Student Account",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Join your verified South African campus community",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            ErrorBanner(
                errorMessage = errorMessage,
                onDismiss = { viewModel.dismissError() },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Legal or Preferred Name *") },
            placeholder = { Text("e.g. Sipho Ndlovu") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_fullname_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email Address
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address *") },
            placeholder = { Text("e.g. student@wits.ac.za") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 characters) *") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password *") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_confirm_password_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Academic Information",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Institution Selector Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCampus.institutionName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Institution / University *") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        modifier = Modifier.clickable { campusDropdownExpanded = true }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { campusDropdownExpanded = true }
                    .testTag("register_institution_dropdown")
            )

            DropdownMenu(
                expanded = campusDropdownExpanded,
                onDismissRequest = { campusDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                SouthAfricanCampus.entries.forEach { campus ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(campus.institutionName, fontWeight = FontWeight.SemiBold)
                                Text("${campus.shortName} • ${campus.province}", style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        onClick = {
                            selectedCampus = campus
                            campusBranchName = "${campus.shortName} Main Campus"
                            campusDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Specific Campus Branch Name
        OutlinedTextField(
            value = campusBranchName,
            onValueChange = { campusBranchName = it },
            label = { Text("Campus Location / Branch *") },
            placeholder = { Text("e.g. Hatfield Campus / Kingsway Campus") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_campus_branch_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Course / Qualification
        OutlinedTextField(
            value = course,
            onValueChange = { course = it },
            label = { Text("Course or Qualification *") },
            placeholder = { Text("e.g. BSc Computer Science / BCom Accounting") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_course_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Year of Study Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = when (yearOfStudy) {
                    1 -> "1st Year (First-year)"
                    2 -> "2nd Year"
                    3 -> "3rd Year"
                    4 -> "4th Year / Honours"
                    5 -> "Masters Degree"
                    6 -> "Doctorate / PhD"
                    else -> "Postgraduate"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Year of Study *") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select Year",
                        modifier = Modifier.clickable { yearDropdownExpanded = true }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { yearDropdownExpanded = true }
                    .testTag("register_year_dropdown")
            )

            DropdownMenu(
                expanded = yearDropdownExpanded,
                onDismissRequest = { yearDropdownExpanded = false }
            ) {
                listOf(
                    1 to "1st Year (First-year)",
                    2 to "2nd Year",
                    3 to "3rd Year",
                    4 to "4th Year / Honours",
                    5 to "Masters Degree",
                    6 to "Doctorate / PhD"
                ).forEach { (year, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            yearOfStudy = year
                            yearDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student Number (Optional)
        OutlinedTextField(
            value = studentNumber,
            onValueChange = { studentNumber = it },
            label = { Text("Student Number (Optional initially)") },
            placeholder = { Text("e.g. 219084729") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_student_number_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        CampusPlugPrimaryButton(
            text = "Create Student Account",
            onClick = {
                focusManager.clearFocus()
                viewModel.registerStudent(
                    fullName = fullName,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    institutionName = selectedCampus.institutionName,
                    campusName = campusBranchName,
                    campusCode = selectedCampus.code,
                    studentNumber = studentNumber,
                    course = course,
                    yearOfStudy = yearOfStudy
                )
            },
            isLoading = isLoading,
            enabled = !isLoading,
            testTag = "register_submit_button"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.testTag("register_login_link_button")
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
