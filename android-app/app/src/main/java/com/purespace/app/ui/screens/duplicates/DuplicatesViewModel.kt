package com.purespace.app.ui.screens.duplicates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purespace.app.domain.model.DuplicateGroup
import com.purespace.app.data.repository.FileRepositoryImpl
import com.purespace.app.data.repository.RemoteFileRepository
import com.purespace.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DuplicatesViewModel @Inject constructor(
    private val fileRepository: FileRepositoryImpl,
    private val remoteFileRepository: RemoteFileRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuplicatesUiState())
    val uiState: StateFlow<DuplicatesUiState> = _uiState.asStateFlow()

    fun loadDuplicates() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val duplicateGroups = fileRepository.detectDuplicates()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    duplicateGroups = duplicateGroups
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load duplicates")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun toggleFileSelection(file: FileItem, isSelected: Boolean) {
        val currentSelected = _uiState.value.selectedFiles.toMutableSet()
        
        if (isSelected) {
            currentSelected.add(file.id)
        } else {
            currentSelected.remove(file.id)
        }
        
        _uiState.value = _uiState.value.copy(selectedFiles = currentSelected)
    }

    fun selectAllInGroup(files: List<FileItem>) {
        val currentSelected = _uiState.value.selectedFiles.toMutableSet()
        files.forEach { file ->
            currentSelected.add(file.id)
        }
        
        _uiState.value = _uiState.value.copy(selectedFiles = currentSelected)
    }

    fun deleteSelectedFiles() {
        // TODO: Implement file deletion
        Timber.d("Delete selected files: ${_uiState.value.selectedFiles}")
    }
    
    fun showPaywall() {
        // TODO: Navigate to paywall screen
        Timber.d("Show paywall for premium features")
    }

    fun markFilesAsDeleted() {
        viewModelScope.launch {
            try {
                val selectedFileIds = _uiState.value.selectedFiles.toList()
                fileRepository.markFilesAsDeleted(selectedFileIds)
                
                // Reload duplicates after deletion
                loadDuplicates()
                
                // Clear selection
                _uiState.value = _uiState.value.copy(selectedFiles = emptySet())
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete selected files")
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class DuplicatesUiState(
    val isLoading: Boolean = false,
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val selectedFiles: Set<String> = emptySet(),
    val error: String? = null
)
