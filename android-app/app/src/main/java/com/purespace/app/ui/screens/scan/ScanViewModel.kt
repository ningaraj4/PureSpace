package com.purespace.app.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purespace.app.domain.usecase.DetectDuplicatesUseCase
import com.purespace.app.domain.usecase.ScanDeviceMediaUseCase
import com.purespace.app.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanDeviceMediaUseCase: ScanDeviceMediaUseCase,
    private val detectDuplicatesUseCase: DetectDuplicatesUseCase,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        startScan()
    }

    private fun startScan() {
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                
                _uiState.value = _uiState.value.copy(
                    isScanning = true,
                    currentStep = "Scanning device files...",
                    progress = 0.1f
                )

                // Step 1: Scan device files
                val scannedFiles = scanDeviceMediaUseCase()
                _uiState.value = _uiState.value.copy(
                    filesScanned = scannedFiles.size,
                    currentStep = "Computing file hashes...",
                    progress = 0.3f,
                    steps = _uiState.value.steps.map { step ->
                        if (step.name == "Scanning device files...") {
                            step.copy(isCompleted = true)
                        } else step
                    }
                )

                // Step 2: Compute hashes
                val hashedFiles = fileRepository.computeHashes(scannedFiles)
                _uiState.value = _uiState.value.copy(
                    currentStep = "Detecting duplicates...",
                    progress = 0.6f,
                    steps = _uiState.value.steps.map { step ->
                        if (step.name == "Computing file hashes...") {
                            step.copy(isCompleted = true)
                        } else step
                    }
                )

                // Step 3: Save files to database
                fileRepository.insertFiles(hashedFiles)

                // Step 4: Detect duplicates
                val duplicateGroups = detectDuplicatesUseCase()
                val duplicatesFound = duplicateGroups.sumOf { it.count - 1 }
                val potentialSavings = duplicateGroups.sumOf { it.potentialSavings }

                _uiState.value = _uiState.value.copy(
                    currentStep = "Scan completed!",
                    progress = 1.0f,
                    isScanning = false,
                    isCompleted = true,
                    duplicatesFound = duplicatesFound,
                    potentialSavings = potentialSavings,
                    scanDuration = System.currentTimeMillis() - startTime,
                    steps = _uiState.value.steps.map { step ->
                        step.copy(isCompleted = true)
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "Scan failed")
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message,
                    currentStep = "Scan failed"
                )
            }
        }
    }
}

data class ScanUiState(
    val isScanning: Boolean = false,
    val isCompleted: Boolean = false,
    val currentStep: String = "Preparing scan...",
    val progress: Float = 0f,
    val filesScanned: Int = 0,
    val duplicatesFound: Int = 0,
    val potentialSavings: Long = 0L,
    val scanDuration: Long = 0L,
    val error: String? = null,
    val steps: List<ScanStep> = listOf(
        ScanStep("Scanning device files...", false),
        ScanStep("Computing file hashes...", false),
        ScanStep("Detecting duplicates...", false),
        ScanStep("Analyzing results...", false)
    )
)

data class ScanStep(
    val name: String,
    val isCompleted: Boolean
)
