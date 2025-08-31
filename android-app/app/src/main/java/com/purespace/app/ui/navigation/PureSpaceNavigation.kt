package com.purespace.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.purespace.app.ui.screens.dashboard.DashboardScreen
import com.purespace.app.ui.screens.duplicates.DuplicatesScreen
import com.purespace.app.ui.screens.largefiles.LargeFilesScreen
import com.purespace.app.ui.screens.onboarding.OnboardingScreen
import com.purespace.app.ui.screens.scan.ScanScreen
import com.purespace.app.ui.screens.settings.SettingsScreen
import com.purespace.app.ui.screens.auth.AuthScreen
import com.purespace.app.ui.screens.paywall.PaywallScreen

@Composable
fun PureSpaceNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToScan = {
                    navController.navigate(Screen.Scan.route)
                },
                onNavigateToDuplicates = {
                    navController.navigate(Screen.Duplicates.route)
                },
                onNavigateToLargeFiles = {
                    navController.navigate(Screen.LargeFiles.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScanComplete = {
                    navController.navigate(Screen.Duplicates.route)
                }
            )
        }
        
        composable(Screen.Duplicates.route) {
            DuplicatesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.LargeFiles.route) {
            LargeFilesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        
        composable(Screen.Paywall.route) {
            PaywallScreen(
                onDismiss = {
                    navController.popBackStack()
                },
                onPurchaseSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
