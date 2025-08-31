package com.purespace.app.util

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest

object HashingUtils {
    
    private const val DEFAULT_BUFFER_SIZE = 8192 * 4 // 32KB buffer for better performance
    
    /**
     * Computes SHA-256 hash of a file from its URI
     */
    suspend fun computeSha256(contentResolver: ContentResolver, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                computeSha256FromStream(inputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to compute SHA-256 for URI: $uri")
            null
        }
    }
    
    /**
     * Computes SHA-256 hash from an InputStream
     */
    private fun computeSha256FromStream(inputStream: InputStream): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digestInputStream = DigestInputStream(inputStream, messageDigest)
        
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (digestInputStream.read(buffer) != -1) {
            // Reading the stream updates the digest
        }
        
        return messageDigest.digest().joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
    
    /**
     * Computes MD5 hash as fallback (faster but less secure)
     */
    suspend fun computeMd5(contentResolver: ContentResolver, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val messageDigest = MessageDigest.getInstance("MD5")
                val digestInputStream = DigestInputStream(inputStream, messageDigest)
                
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (digestInputStream.read(buffer) != -1) {
                    // Reading the stream updates the digest
                }
                
                messageDigest.digest().joinToString("") { byte ->
                    "%02x".format(byte)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to compute MD5 for URI: $uri")
            null
        }
    }
    
    /**
     * Validates if a hash string is a valid SHA-256 hash
     */
    fun isValidSha256(hash: String): Boolean {
        return hash.matches(Regex("^[a-fA-F0-9]{64}$"))
    }
    
    /**
     * Validates if a hash string is a valid MD5 hash
     */
    fun isValidMd5(hash: String): Boolean {
        return hash.matches(Regex("^[a-fA-F0-9]{32}$"))
    }
}
