package com.purespace.app.ui.screens.duplicates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SelectAll
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
import com.purespace.app.domain.model.DuplicateGroup
import com.purespace.app.domain.model.FileItem
import com.purespace.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicatesScreen(
    onNavigateBack: () -> Unit,
    viewModel: DuplicatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadDuplicates()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.duplicates),
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
                if (uiState.selectedFiles.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.loadDuplicates() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Find Duplicates")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.showPaywall() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Advanced Detection (Premium)")
                    }
                    
                    IconButton(
                        onClick = { viewModel.deleteSelectedFiles() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_selected)
                        )
                    }
                }
            }
        }
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.duplicateGroups.isEmpty() -> {
                NoDuplicatesFound()
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DuplicatesSummaryCard(
                            totalGroups = uiState.duplicateGroups.size,
                            totalDuplicates = uiState.duplicateGroups.sumOf { it.count - 1 },
                            potentialSavings = uiState.duplicateGroups.sumOf { it.potentialSavings },
                            selectedCount = uiState.selectedFiles.size
                        )
                    }
                    
                    items(uiState.duplicateGroups) { group ->
                        DuplicateGroupCard(
                            group = group,
                            selectedFiles = uiState.selectedFiles,
                            onFileSelectionChanged = { file, isSelected ->
                                viewModel.toggleFileSelection(file, isSelected)
                            },
                            onSelectAllInGroup = { files ->
                                viewModel.selectAllInGroup(files)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoDuplicatesFound() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_duplicates_found),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.no_duplicates_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DuplicatesSummaryCard(
    totalGroups: Int,
    totalDuplicates: Int,
    potentialSavings: Long,
    selectedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.duplicates_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = totalGroups.toString(),
                    label = stringResource(R.string.duplicate_groups)
                )
                
                SummaryItem(
                    value = totalDuplicates.toString(),
                    label = stringResource(R.string.duplicate_files)
                )
                
                SummaryItem(
                    value = FormatUtils.formatFileSize(potentialSavings),
                    label = stringResource(R.string.potential_savings)
                )
            }
            
            if (selectedCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Padding(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.selected_files_count, selectedCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
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
private fun DuplicateGroupCard(
    group: DuplicateGroup,
    selectedFiles: Set<String>,
    onFileSelectionChanged: (FileItem, Boolean) -> Unit,
    onSelectAllInGroup: (List<FileItem>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.duplicate_group_title, group.count),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.total_size, FormatUtils.formatFileSize(group.totalSize)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                TextButton(
                    onClick = { onSelectAllInGroup(group.files.drop(1)) } // Skip first file (original)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.select_duplicates))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            group.files.forEachIndexed { index, file ->
                DuplicateFileItem(
                    file = file,
                    isOriginal = index == 0,
                    isSelected = selectedFiles.contains(file.id),
                    onSelectionChanged = { isSelected ->
                        onFileSelectionChanged(file, isSelected)
                    }
                )
                
                if (index < group.files.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DuplicateFileItem(
    file: FileItem,
    isOriginal: Boolean,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected,
                enabled = !isOriginal,
                onValueChange = onSelectionChanged
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File thumbnail/icon
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(file.uri)
                .crossfade(true)
                .build(),
            contentDescription = file.displayName,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = file.displayName ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isOriginal) FontWeight.SemiBold else FontWeight.Normal
            )
            
            Row {
                Text(
                    text = FormatUtils.formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (file.bucket != null) {
                    Text(
                        text = " • ${file.bucket}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isOriginal) {
                Text(
                    text = stringResource(R.string.original_file),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (!isOriginal) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )
        }
    }
}
