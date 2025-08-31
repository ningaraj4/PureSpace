package com.purespace.app.ui.screens.auth

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purespace.app.data.auth.AuthManager
import com.purespace.app.data.auth.AuthState
import com.purespace.app.data.local.preferences.PreferencesManager

@Composable
fun AuthNavigationHandler(
    authManager: AuthManager,
    preferencesManager: PreferencesManager,
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    val authState by authManager.authState.collectAsStateWithLifecycle()
    val isOnboardingCompleted = preferencesManager.isOnboardingCompleted()

    LaunchedEffect(authState, isOnboardingCompleted) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (isOnboardingCompleted) {
                    onAuthenticated()
                } else {
                    // User is authenticated but hasn't completed onboarding
                    onAuthenticated()
                }
            }
            is AuthState.Unauthenticated -> {
                onUnauthenticated()
            }
            is AuthState.Loading -> {
                // Stay on current screen while loading
            }
            is AuthState.FirebaseOnly -> {
                // Firebase auth but no backend JWT - need to re-authenticate
                onUnauthenticated()
            }
        }
    }
}
