package com.purespace.app.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "purespace_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val regularPrefs: SharedPreferences = context.getSharedPreferences(
        "purespace_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        // Secure preferences keys (encrypted)
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PROVIDER = "user_provider"
        
        // Regular preferences keys
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_SCAN_FREQUENCY = "scan_frequency"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LARGE_FILE_THRESHOLD = "large_file_threshold"
        private const val KEY_LAST_SCAN_TIME = "last_scan_time"
        private const val KEY_DEVICE_ID = "device_id"
    }
    
    // Authentication data (encrypted)
    fun saveJwtToken(token: String) {
        encryptedPrefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }
    
    fun getJwtToken(): String? = encryptedPrefs.getString(KEY_JWT_TOKEN, null)
    
    fun saveUserInfo(userId: String, email: String, provider: String) {
        encryptedPrefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PROVIDER, provider)
            .apply()
    }
    
    fun getUserId(): String? = encryptedPrefs.getString(KEY_USER_ID, null)
    
    fun getUserEmail(): String? = encryptedPrefs.getString(KEY_USER_EMAIL, null)
    
    fun getUserProvider(): String? = encryptedPrefs.getString(KEY_USER_PROVIDER, null)
    
    fun clearAuthData() {
        encryptedPrefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_PROVIDER)
            .apply()
    }
    
    // App preferences (regular)
    fun setOnboardingCompleted(completed: Boolean) {
        regularPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    fun isOnboardingCompleted(): Boolean = regularPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    
    fun setScanFrequency(frequency: ScanFrequency) {
        regularPrefs.edit().putString(KEY_SCAN_FREQUENCY, frequency.name).apply()
    }
    
    fun getScanFrequency(): ScanFrequency {
        val name = regularPrefs.getString(KEY_SCAN_FREQUENCY, ScanFrequency.WEEKLY.name)
        return ScanFrequency.valueOf(name ?: ScanFrequency.WEEKLY.name)
    }
    
    fun setAutoSync(enabled: Boolean) {
        regularPrefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }
    
    fun isAutoSyncEnabled(): Boolean = regularPrefs.getBoolean(KEY_AUTO_SYNC, true)
    
    fun setNotificationsEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun areNotificationsEnabled(): Boolean = regularPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    
    fun setDarkMode(enabled: Boolean) {
        regularPrefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    fun isDarkModeEnabled(): Boolean = regularPrefs.getBoolean(KEY_DARK_MODE, false)
    
    fun setLargeFileThreshold(threshold: Long) {
        regularPrefs.edit().putLong(KEY_LARGE_FILE_THRESHOLD, threshold).apply()
    }
    
    fun getLargeFileThreshold(): Long = regularPrefs.getLong(KEY_LARGE_FILE_THRESHOLD, 100 * 1024 * 1024) // 100MB default
    
    fun setLastScanTime(timestamp: Long) {
        regularPrefs.edit().putLong(KEY_LAST_SCAN_TIME, timestamp).apply()
    }
    
    fun getLastScanTime(): Long = regularPrefs.getLong(KEY_LAST_SCAN_TIME, 0)
    
    fun getDeviceId(): String {
        var deviceId = regularPrefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = generateDeviceId()
            regularPrefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
    
    private fun generateDeviceId(): String {
        return "device_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

enum class ScanFrequency {
    DAILY, WEEKLY, MONTHLY, MANUAL
}
