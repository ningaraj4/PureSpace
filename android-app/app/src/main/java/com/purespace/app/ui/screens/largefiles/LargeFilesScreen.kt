package com.purespace.app.ui.screens.largefiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purespace.app.R
import com.purespace.app.domain.model.FileItem
import com.purespace.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeFilesScreen(
    onNavigateBack: () -> Unit,
    viewModel: LargeFilesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadLargeFiles()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.large_files),
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
            },
            actions = {
                IconButton(
                    onClick = { viewModel.toggleFilterDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.filter)
                    )
                }
            }
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.largeFiles.isEmpty() -> {
                NoLargeFilesFound()
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        LargeFilesSummaryCard(
                            totalFiles = uiState.largeFiles.size,
                            totalSize = uiState.largeFiles.sumOf { it.size },
                            currentThreshold = uiState.sizeThreshold
                        )
                    }
                    
                    items(uiState.largeFiles) { file ->
                        LargeFileItem(file = file)
                    }
                }
            }
        }
    }
    
    if (uiState.showFilterDialog) {
        FilterDialog(
            currentThreshold = uiState.sizeThreshold,
            onThresholdChanged = { threshold ->
                viewModel.updateSizeThreshold(threshold)
            },
            onDismiss = { viewModel.toggleFilterDialog() }
        )
    }
}

@Composable
private fun NoLargeFilesFound() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_large_files_found),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.no_large_files_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LargeFilesSummaryCard(
    totalFiles: Int,
    totalSize: Long,
    currentThreshold: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.large_files_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = totalFiles.toString(),
                    label = stringResource(R.string.large_files)
                )
                
                SummaryItem(
                    value = FormatUtils.formatFileSize(totalSize),
                    label = stringResource(R.string.total_size)
                )
                
                SummaryItem(
                    value = FormatUtils.formatFileSize(currentThreshold),
                    label = stringResource(R.string.size_threshold)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LargeFileItem(
    file: FileItem
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File thumbnail/icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(file.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = file.displayName,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.displayName ?: "Unknown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = FormatUtils.formatFileSize(file.size),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                if (file.bucket != null) {
                    Text(
                        text = file.bucket,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = file.mediaType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    currentThreshold: Long,
    onThresholdChanged: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val thresholdOptions = listOf(
        10 * 1024 * 1024L to "10 MB",
        50 * 1024 * 1024L to "50 MB",
        100 * 1024 * 1024L to "100 MB",
        500 * 1024 * 1024L to "500 MB",
        1024 * 1024 * 1024L to "1 GB"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.filter_large_files))
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.select_size_threshold),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                thresholdOptions.forEach { (threshold, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentThreshold == threshold,
                            onClick = { onThresholdChanged(threshold) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
