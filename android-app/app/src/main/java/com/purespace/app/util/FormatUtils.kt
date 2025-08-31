package com.purespace.app.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

object FormatUtils {
    
    private val decimalFormat = DecimalFormat("#.##")
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    /**
     * Formats file size in human-readable format (B, KB, MB, GB, TB)
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        
        return "${decimalFormat.format(bytes / 1024.0.pow(digitGroups.toDouble()))} ${units[digitGroups]}"
    }
    
    /**
     * Formats date to human-readable string
     */
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
    
    /**
     * Formats date to relative time (e.g., "2 hours ago", "Yesterday")
     */
    fun formatRelativeTime(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 172800_000 -> "Yesterday"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> formatDate(date)
        }
    }
    
    /**
     * Formats percentage with one decimal place
     */
    fun formatPercentage(value: Float): String {
        return "${decimalFormat.format(value * 100)}%"
    }
    
    /**
     * Formats duration in milliseconds to human-readable format
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    /**
     * Formats file count with proper pluralization
     */
    fun formatFileCount(count: Int): String {
        return if (count == 1) "$count file" else "$count files"
    }
}
