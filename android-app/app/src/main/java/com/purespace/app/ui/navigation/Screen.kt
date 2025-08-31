package com.purespace.app.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Scan : Screen("scan")
    object Duplicates : Screen("duplicates")
    object LargeFiles : Screen("large_files")
    object Settings : Screen("settings")
    object CleanupSummary : Screen("cleanup_summary")
    object History : Screen("history")
    object Paywall : Screen("paywall")
}
