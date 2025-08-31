package com.purespace.app.ui.screens.settings

import com.purespace.app.data.local.preferences.PreferencesManager
import com.purespace.app.data.auth.AuthManager
import com.purespace.app.work.WorkScheduler
import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authManager: AuthManager,
    private val workScheduler: WorkScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleScheduledScans() {
        _uiState.value = _uiState.value.copy(
            scheduledScansEnabled = !_uiState.value.scheduledScansEnabled
        )
    }

    fun toggleWifiOnlyHashing() {
        _uiState.value = _uiState.value.copy(
            wifiOnlyHashing = !_uiState.value.wifiOnlyHashing
        )
    }

    fun toggleScanNotifications() {
        _uiState.value = _uiState.value.copy(
            scanNotificationsEnabled = !_uiState.value.scanNotificationsEnabled
        )
    }

    fun toggleStorageAlerts() {
        _uiState.value = _uiState.value.copy(
            storageAlertsEnabled = !_uiState.value.storageAlertsEnabled
        )
    }

    fun toggleCloudSync() {
        _uiState.value = _uiState.value.copy(
            cloudSyncEnabled = !_uiState.value.cloudSyncEnabled
        )
    }

    fun showThemeDialog() {
        Timber.d("Show theme dialog")
        // TODO: Implement theme selection dialog
    }

    fun showLanguageDialog() {
        Timber.d("Show language dialog")
        // TODO: Implement language selection dialog
    }

    fun signIn() {
        viewModelScope.launch {
            try {
                Timber.d("Sign in requested")
                // TODO: Implement Firebase Auth sign in
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    userEmail = "user@example.com"
                )
            } catch (e: Exception) {
                Timber.e(e, "Sign in failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    userEmail = null,
                    cloudSyncEnabled = false,
                    showSignOutDialog = false
                )
            } catch (e: Exception) {
                Timber.e(e, "Sign out failed")
                _uiState.value = _uiState.value.copy(
                    showSignOutDialog = false
                )
            }
        }
    }

    fun showPremiumDialog() {
        Timber.d("Show premium dialog")
        // TODO: Implement premium upgrade dialog
    }

    fun openPrivacyPolicy() {
        Timber.d("Open privacy policy")
        // TODO: Open privacy policy URL
    }

    fun openSupport() {
        Timber.d("Open support")
        // TODO: Open support/help URL
    }
}

data class SettingsUiState(
    val scheduledScansEnabled: Boolean = false,
    val wifiOnlyHashing: Boolean = true,
    val scanNotificationsEnabled: Boolean = true,
    val storageAlertsEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null
)
