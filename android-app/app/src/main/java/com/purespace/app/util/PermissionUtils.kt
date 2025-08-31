package com.purespace.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {
    
    /**
     * Gets the required storage permissions based on Android version
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * Gets the notification permission for Android 13+
     */
    fun getNotificationPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
    }
    
    /**
     * Checks if all required storage permissions are granted
     */
    fun hasStoragePermissions(context: Context): Boolean {
        return getRequiredStoragePermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Checks if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }
    
    /**
     * Gets all permissions needed by the app
     */
    fun getAllRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        permissions.addAll(getRequiredStoragePermissions())
        
        getNotificationPermission()?.let { permissions.add(it) }
        
        return permissions.toTypedArray()
    }
    
    /**
     * Gets permission rationale text for user explanation
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> 
                "PureSpace needs access to your files to scan for duplicates and large files. We only read file metadata, never the actual content."
            
            Manifest.permission.READ_MEDIA_IMAGES -> 
                "PureSpace needs access to your photos to find duplicate images and free up storage space."
            
            Manifest.permission.READ_MEDIA_VIDEO -> 
                "PureSpace needs access to your videos to find duplicate videos and identify large files."
            
            Manifest.permission.READ_MEDIA_AUDIO -> 
                "PureSpace needs access to your audio files to find duplicate music and audio files."
            
            Manifest.permission.POST_NOTIFICATIONS -> 
                "PureSpace needs notification permission to inform you about scan progress and storage optimization opportunities."
            
            else -> "This permission is required for PureSpace to function properly."
        }
    }
}
