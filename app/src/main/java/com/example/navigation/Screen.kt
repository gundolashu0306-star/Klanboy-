package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for Campus Plug SA.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val showInBottomBar: Boolean = false
) {
    // Authentication flows
    data object Login : Screen("login", "Sign In")
    data object Register : Screen("register", "Register")
    data object ForgotPassword : Screen("forgot_password", "Password Recovery")

    // Main top-level tabs
    data object Dashboard : Screen("dashboard", "Plugs", Icons.Default.ElectricBolt, true)
    data object Campuses : Screen("campuses", "Campuses", Icons.Default.School, true)
    data object Marketplace : Screen("marketplace", "Market", Icons.Default.ShoppingBag, true)
    data object Housing : Screen("housing", "Res & Pads", Icons.Default.Apartment, true)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person, true)

    // Sub-screens
    data object CommunityDetail : Screen("community_detail/{communityId}", "Community Discussion") {
        fun createRoute(communityId: String): String = "community_detail/$communityId"
    }
    data object EditProfile : Screen("edit_profile", "Edit Profile")
    data object StudentVerification : Screen("student_verification", "Student Verification")
    data object Diagnostics : Screen("diagnostics", "Readiness", Icons.Default.Info, false)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Campuses,
    Screen.Marketplace,
    Screen.Housing,
    Screen.Profile
)
