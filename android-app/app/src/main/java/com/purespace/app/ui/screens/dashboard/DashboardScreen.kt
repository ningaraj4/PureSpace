package com.purespace.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.purespace.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToDuplicates: () -> Unit,
    onNavigateToLargeFiles: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Overview Card
            item {
                StorageOverviewCard(
                    totalStorage = uiState.totalStorage,
                    usedStorage = uiState.usedStorage,
                    potentialSavings = uiState.potentialSavings
                )
            }
            
            // Quick Actions
            item {
                QuickActionsSection(
                    onScanClick = onNavigateToScan,
                    onDuplicatesClick = onNavigateToDuplicates,
                    onLargeFilesClick = onNavigateToLargeFiles,
                    duplicateCount = uiState.duplicateCount,
                    largeFileCount = uiState.largeFileCount
                )
            }
            
            // Recent Activity
            item {
                RecentActivityCard(
                    lastScanTime = uiState.lastScanTime,
                    filesScanned = uiState.filesScanned,
                    duplicatesFound = uiState.duplicatesFound
                )
            }
            
            // Storage Tips
            item {
                StorageTipsCard()
            }
        }
    }
}

@Composable
private fun StorageOverviewCard(
    totalStorage: Long,
    usedStorage: Long,
    potentialSavings: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.storage_overview),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Storage Progress Bar
            val usagePercentage = if (totalStorage > 0) {
                (usedStorage.toFloat() / totalStorage.toFloat()).coerceIn(0f, 1f)
            } else 0f
            
            LinearProgressIndicator(
                progress = usagePercentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    usagePercentage > 0.9f -> MaterialTheme.colorScheme.error
                    usagePercentage > 0.7f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.used),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatFileSize(usedStorage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatFileSize(totalStorage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            if (potentialSavings > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = stringResource(R.string.potential_savings),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = FormatUtils.formatFileSize(potentialSavings),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onScanClick: () -> Unit,
    onDuplicatesClick: () -> Unit,
    onLargeFilesClick: () -> Unit,
    duplicateCount: Int,
    largeFileCount: Int
) {
    Column {
        Text(
            text = stringResource(R.string.quick_actions),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.scan_now),
                    subtitle = stringResource(R.string.find_duplicates_and_large_files),
                    onClick = onScanClick
                )
            }
            
            item {
                QuickActionCard(
                    icon = Icons.Default.ContentCopy,
                    title = stringResource(R.string.duplicates),
                    subtitle = if (duplicateCount > 0) {
                        stringResource(R.string.duplicate_count, duplicateCount)
                    } else {
                        stringResource(R.string.no_duplicates_found)
                    },
                    onClick = onDuplicatesClick,
                    badge = if (duplicateCount > 0) duplicateCount.toString() else null
                )
            }
            
            item {
                QuickActionCard(
                    icon = Icons.Default.Storage,
                    title = stringResource(R.string.large_files),
                    subtitle = if (largeFileCount > 0) {
                        stringResource(R.string.large_file_count, largeFileCount)
                    } else {
                        stringResource(R.string.no_large_files_found)
                    },
                    onClick = onLargeFilesClick,
                    badge = if (largeFileCount > 0) largeFileCount.toString() else null
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            badge?.let {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(text = it)
                }
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    lastScanTime: String?,
    filesScanned: Int,
    duplicatesFound: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.recent_activity),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (lastScanTime != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = stringResource(R.string.last_scan, lastScanTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.scan_results, filesScanned, duplicatesFound),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.no_recent_scans),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StorageTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(R.string.storage_tips),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.storage_tip_1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
