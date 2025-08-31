package com.purespace.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purespace.app.domain.repository.FileRepository
import com.purespace.app.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val fileRepository: FileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val stats = getDashboardStatsUseCase()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalStorage = stats.totalSize,
                    usedStorage = stats.totalSize,
                    potentialSavings = stats.potentialSavings,
                    duplicateCount = stats.duplicateFiles,
                    largeFileCount = stats.largeFiles,
                    filesScanned = stats.totalFiles,
                    duplicatesFound = stats.duplicateFiles,
                    lastScanTime = stats.lastScanTime?.let { "Recently" } // Format properly
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load dashboard data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalStorage: Long = 0L,
    val usedStorage: Long = 0L,
    val potentialSavings: Long = 0L,
    val duplicateCount: Int = 0,
    val largeFileCount: Int = 0,
    val filesScanned: Int = 0,
    val duplicatesFound: Int = 0,
    val lastScanTime: String? = null,
    val error: String? = null
)
