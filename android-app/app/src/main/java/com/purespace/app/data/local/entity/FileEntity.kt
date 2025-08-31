package com.purespace.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purespace.app.domain.model.MediaType

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val uri: String,
    val displayName: String?,
    val mimeType: String?,
    val size: Long,
    val bucket: String?,
    val dateModified: Long,
    val sha256: String?,
    val mediaType: MediaType,
    val isDuplicate: Boolean = false,
    val groupHash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val filesScanned: Int,
    val bytesScanned: Long,
    val duplicatesFound: Int,
    val bytesPotentiallySaved: Long
)

@Entity(tableName = "cleanup_actions")
data class CleanupActionEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val actionType: String,
    val bytesFreed: Long,
    val timestamp: Long
)
