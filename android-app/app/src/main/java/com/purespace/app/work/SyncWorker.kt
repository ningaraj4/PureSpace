package com.purespace.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.purespace.app.data.auth.AuthManager
import com.purespace.app.data.auth.AuthState
import com.purespace.app.data.local.preferences.PreferencesManager
import com.purespace.app.data.repository.FileRepositoryImpl
import com.purespace.app.data.repository.RemoteFileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileRepository: FileRepositoryImpl,
    private val remoteFileRepository: RemoteFileRepository,
    private val authManager: AuthManager,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "sync_work"
        const val TAG = "SyncWorker"
        
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            return PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6, // 6 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 1, // 1 hour flex
                flexTimeIntervalUnit = TimeUnit.HOURS
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
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting sync work")
            
            // Check if user is authenticated
            val authState = authManager.authState.first()
            if (authState !is AuthState.Authenticated) {
                Timber.w("User not authenticated, skipping sync")
                return Result.success()
            }
            
            // Check if auto-sync is enabled
            if (!preferencesManager.isAutoSyncEnabled()) {
                Timber.d("Auto-sync disabled, skipping sync")
                return Result.success()
            }
            
            // Get all local files
            val localFiles = fileRepository.getAllFiles().first()
            Timber.d("Found ${localFiles.size} local files to sync")
            
            if (localFiles.isNotEmpty()) {
                // Upload metadata to backend
                val deviceId = preferencesManager.getDeviceId()
                val uploadResult = remoteFileRepository.uploadMetadata(deviceId, localFiles)
                
                if (uploadResult.isSuccess) {
                    // Update last sync time
                    preferencesManager.setLastScanTime(System.currentTimeMillis())
                    Timber.d("Sync completed successfully")
                    Result.success()
                } else {
                    val error = uploadResult.exceptionOrNull()
                    Timber.e(error, "Sync failed")
                    Result.retry()
                }
            } else {
                Timber.d("No files to sync")
                Result.success()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Sync work failed")
            Result.retry()
        }
    }
}
