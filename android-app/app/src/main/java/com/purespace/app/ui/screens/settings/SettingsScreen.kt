package com.purespace.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.purespace.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings),
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSection(title = stringResource(R.string.scanning_preferences))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = stringResource(R.string.scheduled_scans),
                    subtitle = stringResource(R.string.scheduled_scans_desc),
                    trailing = {
                        Switch(
                            checked = uiState.scheduledScansEnabled,
                            onCheckedChange = { viewModel.toggleScheduledScans() }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Wifi,
                    title = stringResource(R.string.wifi_only_hashing),
                    subtitle = stringResource(R.string.wifi_only_hashing_desc),
                    trailing = {
                        Switch(
                            checked = uiState.wifiOnlyHashing,
                            onCheckedChange = { viewModel.toggleWifiOnlyHashing() }
                        )
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.appearance))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.theme),
                    subtitle = stringResource(R.string.theme_desc),
                    onClick = { viewModel.showThemeDialog() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = stringResource(R.string.language_desc),
                    onClick = { viewModel.showLanguageDialog() }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.notifications))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.scan_notifications),
                    subtitle = stringResource(R.string.scan_notifications_desc),
                    trailing = {
                        Switch(
                            checked = uiState.scanNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleScanNotifications() }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = stringResource(R.string.storage_alerts),
                    subtitle = stringResource(R.string.storage_alerts_desc),
                    trailing = {
                        Switch(
                            checked = uiState.storageAlertsEnabled,
                            onCheckedChange = { viewModel.toggleStorageAlerts() }
                        )
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.account_and_sync))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = stringResource(R.string.account),
                    subtitle = if (uiState.isLoggedIn) {
                        stringResource(R.string.signed_in_as, uiState.userEmail ?: "")
                    } else {
                        stringResource(R.string.sign_in_to_sync)
                    },
                    onClick = { 
                        if (uiState.isLoggedIn) {
                            viewModel.signOut()
                        } else {
                            viewModel.signIn()
                        }
                    }
                )
            }
            
            if (uiState.isLoggedIn) {
                item {
                    SettingsItem(
                        icon = Icons.Default.CloudSync,
                        title = stringResource(R.string.cloud_sync),
                        subtitle = stringResource(R.string.cloud_sync_desc),
                        trailing = {
                            Switch(
                                checked = uiState.cloudSyncEnabled,
                                onCheckedChange = { viewModel.toggleCloudSync() }
                            )
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.premium))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.upgrade_to_premium),
                    subtitle = stringResource(R.string.premium_features_desc),
                    onClick = { viewModel.showPremiumDialog() }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.about))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.app_version),
                    subtitle = "1.0.0"
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = stringResource(R.string.privacy_policy),
                    onClick = { viewModel.openPrivacyPolicy() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = stringResource(R.string.help_and_support),
                    onClick = { viewModel.openSupport() }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (trailing != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailing()
            }
        }
    }
}
