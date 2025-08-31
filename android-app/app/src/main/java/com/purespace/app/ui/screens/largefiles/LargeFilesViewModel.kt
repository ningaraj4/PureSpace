package com.purespace.app.ui.screens.largefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purespace.app.domain.model.FileItem
import com.purespace.app.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LargeFilesViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LargeFilesUiState())
    val uiState: StateFlow<LargeFilesUiState> = _uiState.asStateFlow()

    fun loadLargeFiles() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                fileRepository.getLargeFiles(_uiState.value.sizeThreshold).collect { files ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        largeFiles = files.sortedByDescending { it.size }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load large files")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateSizeThreshold(threshold: Long) {
        _uiState.value = _uiState.value.copy(sizeThreshold = threshold)
        loadLargeFiles()
    }

    fun toggleFilterDialog() {
        _uiState.value = _uiState.value.copy(
            showFilterDialog = !_uiState.value.showFilterDialog
        )
    }
}

data class LargeFilesUiState(
    val isLoading: Boolean = false,
    val largeFiles: List<FileItem> = emptyList(),
    val sizeThreshold: Long = 50 * 1024 * 1024L, // 50MB default
    val showFilterDialog: Boolean = false,
    val error: String? = null
)
