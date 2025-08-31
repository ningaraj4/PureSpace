package com.purespace.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.purespace.app.data.local.preferences.PreferencesManager
import com.purespace.app.data.local.preferences.ScanFrequency
import com.purespace.app.data.repository.FileRepositoryImpl
import com.purespace.app.util.PermissionUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class ScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileRepository: FileRepositoryImpl,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "scan_work"
        const val TAG = "ScanWorker"
        
        fun createPeriodicWorkRequest(frequency: ScanFrequency): PeriodicWorkRequest {
            val (repeatInterval, timeUnit) = when (frequency) {
                ScanFrequency.DAILY -> 1L to TimeUnit.DAYS
                ScanFrequency.WEEKLY -> 7L to TimeUnit.DAYS
                ScanFrequency.MONTHLY -> 30L to TimeUnit.DAYS
                ScanFrequency.MANUAL -> return createOneTimeWorkRequest() as PeriodicWorkRequest // This won't be used
            }
            
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true) // Scan when device is idle
                .build()

            return PeriodicWorkRequestBuilder<ScanWorker>(
                repeatInterval = repeatInterval,
                repeatIntervalTimeUnit = timeUnit
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
        
        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            return OneTimeWorkRequestBuilder<ScanWorker>()
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting background scan")
            
            // Check if we have storage permissions
            if (!PermissionUtils.hasStoragePermissions(applicationContext)) {
                Timber.w("Storage permissions not granted, skipping scan")
                return Result.success()
            }
            
            // Update progress
            setProgress(workDataOf("progress" to 0, "status" to "Starting scan..."))
            
            // Scan device for media files
            val scanResult = fileRepository.scanDeviceMedia()
            
            scanResult.collect { progress ->
                when (progress) {
                    is com.purespace.app.domain.model.ScanProgress.InProgress -> {
                        setProgress(workDataOf(
                            "progress" to progress.percentage,
                            "status" to progress.currentStep,
                            "files_found" to progress.filesScanned
                        ))
                    }
                    is com.purespace.app.domain.model.ScanProgress.Completed -> {
                        Timber.d("Background scan completed: ${progress.totalFiles} files found")
                        
                        // Update last scan time
                        preferencesManager.setLastScanTime(System.currentTimeMillis())
                        
                        // Set final progress
                        setProgress(workDataOf(
                            "progress" to 100,
                            "status" to "Scan completed",
                            "files_found" to progress.totalFiles
                        ))
                        
                        return@collect
                    }
                    is com.purespace.app.domain.model.ScanProgress.Error -> {
                        Timber.e("Background scan failed: ${progress.message}")
                        return Result.failure(
                            workDataOf("error" to progress.message)
                        )
                    }
                }
            }
            
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Background scan failed")
            Result.failure(workDataOf("error" to e.message))
        }
    }
}
