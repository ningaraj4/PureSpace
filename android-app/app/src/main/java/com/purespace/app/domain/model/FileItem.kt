package com.purespace.app.domain.model

import java.util.Date

data class FileItem(
    val id: String,
    val uri: String,
    val displayName: String?,
    val mimeType: String?,
    val size: Long,
    val bucket: String?,
    val dateModified: Date,
    val sha256: String?,
    val mediaType: MediaType,
    val isDuplicate: Boolean = false,
    val groupHash: String? = null
)

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    APK,
    OTHER
}

data class DuplicateGroup(
    val sha256: String,
    val files: List<FileItem>,
    val totalSize: Long,
    val count: Int
) {
    val potentialSavings: Long = totalSize - (if (files.isNotEmpty()) files[0].size else 0L)
}

data class ScanSession(
    val id: String,
    val startedAt: Date,
    val finishedAt: Date?,
    val filesScanned: Int,
    val bytesScanned: Long,
    val duplicatesFound: Int,
    val bytesPotentiallySaved: Long
)

data class CleanupAction(
    val id: String,
    val sessionId: String,
    val actionType: ActionType,
    val bytesFreed: Long,
    val timestamp: Date
)

enum class ActionType {
    DELETE,
    ARCHIVE,
    MOVE,
    SHARE
}

data class Stats(
    val totalFiles: Int,
    val totalSize: Long,
    val duplicateFiles: Int,
    val duplicateSize: Long,
    val largeFiles: Int,
    val largeFilesSize: Long,
    val potentialSavings: Long,
    val lastScanTime: Date?
)
