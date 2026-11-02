package com.example.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirebaseConfig
import com.example.data.firebase.FirestoreService
import com.example.data.firebase.StorageRepository
import com.example.ui.components.CampusPlugTopBar
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.campuses.CampusesScreen
import com.example.ui.screens.campuses.CommunityDiscussionScreen
import com.example.ui.screens.campuses.CommunityViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.feed.FeedViewModel
import com.example.ui.screens.housing.HousingScreen
import com.example.ui.screens.marketplace.MarketplaceScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.profile.StudentVerificationScreen
import com.example.ui.screens.readiness.FirebaseReadinessScreen

@Composable
fun CampusPlugAppRoot(
    authRepository: AuthRepository,
    firestoreService: FirestoreService,
    storageRepository: StorageRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

    val context = LocalContext.current
    val readinessReport = FirebaseConfig.evaluateReadiness(context)

    val currentUser by authRepository.observeAuthState().collectAsState(initial = authRepository.currentUser)

    val authViewModel = remember { AuthViewModel(authRepository, firestoreService) }
    val profileViewModel = remember { ProfileViewModel(authRepository, firestoreService, storageRepository) }
    val communityViewModel = remember { CommunityViewModel(authRepository, firestoreService) }
    val feedViewModel = remember { FeedViewModel(authRepository, firestoreService, storageRepository) }

    val isTopLevelRoute = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (isTopLevelRoute) {
                CampusPlugTopBar(
                    title = "Campus Plug",
                    subtitle = "SA",
                    isBackendReady = readinessReport.isFullyReady,
                    onDiagnosticsClick = {
                        navController.navigate(Screen.Diagnostics.route)
                    }
                )
            }
        },
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("campus_plug_bottom_navigation")
                ) {
                    BottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen.icon != null) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Authentication Destinations
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onNavigateBackToLogin = { navController.popBackStack() }
                )
            }

            // Main Tabs
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    feedViewModel = feedViewModel,
                    onNavigateToCategory = { category ->
                        when (category) {
                            com.example.data.model.CommunityCategory.ACCOMMODATION ->
                                navController.navigate(Screen.Housing.route)
                            com.example.data.model.CommunityCategory.STUDENT_MARKET ->
                                navController.navigate(Screen.Marketplace.route)
                            else ->
                                navController.navigate(Screen.Campuses.route)
                        }
                    },
                    onNavigateToDiagnostics = {
                        navController.navigate(Screen.Diagnostics.route)
                    }
                )
            }

            composable(Screen.Campuses.route) {
                CampusesScreen(
                    viewModel = communityViewModel,
                    onNavigateToCommunity = { communityId ->
                        navController.navigate(Screen.CommunityDetail.createRoute(communityId))
                    }
                )
            }

            composable(
                route = Screen.CommunityDetail.route,
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                CommunityDiscussionScreen(
                    communityId = communityId,
                    viewModel = communityViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    onNavigateToReadiness = { navController.navigate(Screen.Diagnostics.route) }
                )
            }

            composable(Screen.Housing.route) {
                HousingScreen(
                    onNavigateToReadiness = { navController.navigate(Screen.Diagnostics.route) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToVerification = { navController.navigate(Screen.StudentVerification.route) },
                    onSignedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Profile Sub-Screens
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.StudentVerification.route) {
                StudentVerificationScreen(
                    viewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Diagnostics.route) {
                FirebaseReadinessScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
