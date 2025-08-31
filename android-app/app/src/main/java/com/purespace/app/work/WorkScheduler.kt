package com.purespace.app.work

import androidx.work.*
import com.purespace.app.data.local.preferences.PreferencesManager
import com.purespace.app.data.local.preferences.ScanFrequency
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val preferencesManager: PreferencesManager
) {

    fun schedulePeriodicScan() {
        val frequency = preferencesManager.getScanFrequency()
        
        if (frequency == ScanFrequency.MANUAL) {
            Timber.d("Manual scan frequency selected, cancelling periodic scans")
            cancelPeriodicScan()
            return
        }
        
        val workRequest = ScanWorker.createPeriodicWorkRequest(frequency)
        
        workManager.enqueueUniquePeriodicWork(
            ScanWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
        
        Timber.d("Scheduled periodic scan with frequency: $frequency")
    }
    
    fun scheduleOneTimeScan() {
        val workRequest = ScanWorker.createOneTimeWorkRequest()
        
        workManager.enqueueUniqueWork(
            "${ScanWorker.WORK_NAME}_onetime",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Timber.d("Scheduled one-time scan")
    }
    
    fun cancelPeriodicScan() {
        workManager.cancelUniqueWork(ScanWorker.WORK_NAME)
        Timber.d("Cancelled periodic scan")
    }
    
    fun schedulePeriodicSync() {
        if (!preferencesManager.isAutoSyncEnabled()) {
            Timber.d("Auto-sync disabled, cancelling periodic sync")
            cancelPeriodicSync()
            return
        }
        
        val workRequest = SyncWorker.createPeriodicWorkRequest()
        
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
        
        Timber.d("Scheduled periodic sync")
    }
    
    fun scheduleOneTimeSync() {
        val workRequest = SyncWorker.createOneTimeWorkRequest()
        
        workManager.enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_onetime",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Timber.d("Scheduled one-time sync")
    }
    
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        Timber.d("Cancelled periodic sync")
    }
    
    fun cancelAllWork() {
        workManager.cancelAllWorkByTag(ScanWorker.TAG)
        workManager.cancelAllWorkByTag(SyncWorker.TAG)
        Timber.d("Cancelled all background work")
    }
    
    fun getWorkInfo(workName: String) = workManager.getWorkInfosForUniqueWork(workName)
}
