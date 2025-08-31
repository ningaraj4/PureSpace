package com.purespace.app.util

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.purespace.app.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object MediaStoreUtils {
    
    data class MediaFile(
        val id: Long,
        val uri: Uri,
        val displayName: String?,
        val mimeType: String?,
        val size: Long,
        val bucket: String?,
        val dateModified: Long,
        val mediaType: MediaType
    )
    
    /**
     * Scans all media files using MediaStore API
     */
    suspend fun scanAllMediaFiles(contentResolver: ContentResolver): List<MediaFile> = withContext(Dispatchers.IO) {
        val allFiles = mutableListOf<MediaFile>()
        
        // Scan images
        allFiles.addAll(scanMediaType(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaType.IMAGE))
        
        // Scan videos
        allFiles.addAll(scanMediaType(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaType.VIDEO))
        
        // Scan audio
        allFiles.addAll(scanMediaType(contentResolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaType.AUDIO))
        
        // Scan documents (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                allFiles.addAll(scanMediaType(contentResolver, MediaStore.Files.getContentUri("external"), MediaType.DOCUMENT))
            } catch (e: Exception) {
                Timber.w(e, "Failed to scan documents")
            }
        }
        
        allFiles
    }
    
    /**
     * Scans a specific media type
     */
    private suspend fun scanMediaType(
        contentResolver: ContentResolver,
        uri: Uri,
        mediaType: MediaType
    ): List<MediaFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<MediaFile>()
        
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        
        val selection = when (mediaType) {
            MediaType.DOCUMENT -> "${MediaStore.MediaColumns.MIME_TYPE} LIKE ? OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE ?"
            else -> null
        }
        
        val selectionArgs = when (mediaType) {
            MediaType.DOCUMENT -> arrayOf("application/%", "text/%")
            else -> null
        }
        
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        
        try {
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(nameColumn)
                    val mimeType = cursor.getString(mimeColumn)
                    val size = cursor.getLong(sizeColumn)
                    val bucket = cursor.getString(bucketColumn)
                    val dateModified = cursor.getLong(dateColumn)
                    
                    // Skip very small files (likely thumbnails or system files)
                    if (size < 1024) continue
                    
                    val contentUri = ContentUris.withAppendedId(uri, id)
                    val detectedMediaType = detectMediaType(mimeType, displayName)
                    
                    files.add(
                        MediaFile(
                            id = id,
                            uri = contentUri,
                            displayName = displayName,
                            mimeType = mimeType,
                            size = size,
                            bucket = bucket,
                            dateModified = dateModified * 1000, // Convert to milliseconds
                            mediaType = detectedMediaType
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to scan media type: $mediaType")
        }
        
        files
    }
    
    /**
     * Detects media type from MIME type and file name
     */
    private fun detectMediaType(mimeType: String?, displayName: String?): MediaType {
        return when {
            mimeType?.startsWith("image/") == true -> MediaType.IMAGE
            mimeType?.startsWith("video/") == true -> MediaType.VIDEO
            mimeType?.startsWith("audio/") == true -> MediaType.AUDIO
            mimeType?.startsWith("application/") == true -> {
                when {
                    displayName?.endsWith(".apk", ignoreCase = true) == true -> MediaType.APK
                    else -> MediaType.DOCUMENT
                }
            }
            mimeType?.startsWith("text/") == true -> MediaType.DOCUMENT
            displayName?.endsWith(".apk", ignoreCase = true) == true -> MediaType.APK
            else -> MediaType.OTHER
        }
    }
    
    /**
     * Gets file size threshold for "large files" based on media type
     */
    fun getLargeFileThreshold(mediaType: MediaType): Long {
        return when (mediaType) {
            MediaType.IMAGE -> 10 * 1024 * 1024 // 10MB
            MediaType.VIDEO -> 100 * 1024 * 1024 // 100MB
            MediaType.AUDIO -> 50 * 1024 * 1024 // 50MB
            MediaType.DOCUMENT -> 20 * 1024 * 1024 // 20MB
            MediaType.APK -> 50 * 1024 * 1024 // 50MB
            MediaType.OTHER -> 10 * 1024 * 1024 // 10MB
        }
    }
}
