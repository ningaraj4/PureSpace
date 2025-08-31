package com.purespace.app.data.repository

import android.content.ContentResolver
import com.purespace.app.data.local.dao.FileDao
import com.purespace.app.data.local.entity.FileEntity
import com.purespace.app.domain.model.*
import com.purespace.app.domain.repository.FileRepository
import com.purespace.app.util.HashingUtils
import com.purespace.app.util.MediaStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val fileDao: FileDao,
    private val contentResolver: ContentResolver
) : FileRepository {

    override fun getAllFiles(): Flow<List<FileItem>> {
        return fileDao.getAllFiles().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFilesByType(mediaType: MediaType): Flow<List<FileItem>> {
        return fileDao.getFilesByType(mediaType).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getLargeFiles(minSize: Long): Flow<List<FileItem>> {
        return fileDao.getLargeFiles(minSize).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDuplicateGroups(): Flow<List<DuplicateGroup>> {
        return fileDao.getDuplicateGroups().map { groupInfos ->
            groupInfos.mapNotNull { groupInfo ->
                val files = fileDao.getFilesByHash(groupInfo.sha256)
                    .map { entities -> entities.map { it.toDomainModel() } }
                
                // This is a simplified version - in real implementation,
                // you'd need to collect the flow properly
                if (groupInfo.count > 1) {
                    DuplicateGroup(
                        sha256 = groupInfo.sha256,
                        files = emptyList(), // Would be populated from flow
                        totalSize = groupInfo.totalSize,
                        count = groupInfo.count
                    )
                } else null
            }
        }
    }

    override suspend fun insertFiles(files: List<FileItem>) {
        withContext(Dispatchers.IO) {
            val entities = files.map { it.toEntity() }
            fileDao.insertFiles(entities)
        }
    }

    override suspend fun updateFile(file: FileItem) {
        withContext(Dispatchers.IO) {
            fileDao.updateFile(file.toEntity())
        }
    }

    override suspend fun markFilesAsDeleted(fileIds: List<String>) {
        withContext(Dispatchers.IO) {
            fileDao.markFilesAsDeleted(fileIds)
        }
    }

    override suspend fun getStats(): Stats {
        return withContext(Dispatchers.IO) {
            val totalFiles = fileDao.getTotalFileCount()
            val totalSize = fileDao.getTotalSize() ?: 0L
            val duplicateFiles = fileDao.getDuplicateFileCount()
            val duplicateSize = fileDao.getDuplicateSize() ?: 0L
            
            // Calculate large files (files > 50MB)
            val largeFileThreshold = 50 * 1024 * 1024L
            val largeFiles = fileDao.getLargeFiles(largeFileThreshold)
            val largeFileCount = largeFiles.map { it.size }.size
            val largeFilesSize = largeFiles.map { entities -> 
                entities.sumOf { it.size } 
            }
            
            Stats(
                totalFiles = totalFiles,
                totalSize = totalSize,
                duplicateFiles = duplicateFiles,
                duplicateSize = duplicateSize,
                largeFiles = largeFileCount,
                largeFilesSize = 0L, // Would need proper flow collection
                potentialSavings = duplicateSize,
                lastScanTime = null // Would get from scan session
            )
        }
    }

    override suspend fun scanDeviceFiles(): List<FileItem> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Starting device file scan")
                val mediaFiles = MediaStoreUtils.scanAllMediaFiles(contentResolver)
                
                val fileItems = mediaFiles.map { mediaFile ->
                    FileItem(
                        id = mediaFile.id.toString(),
                        uri = mediaFile.uri.toString(),
                        displayName = mediaFile.displayName,
                        mimeType = mediaFile.mimeType,
                        size = mediaFile.size,
                        bucket = mediaFile.bucket,
                        dateModified = Date(mediaFile.dateModified),
                        sha256 = null, // Will be computed later
                        mediaType = mediaFile.mediaType
                    )
                }
                
                Timber.d("Scanned ${fileItems.size} files")
                fileItems
            } catch (e: Exception) {
                Timber.e(e, "Failed to scan device files")
                emptyList()
            }
        }
    }

    override suspend fun computeHashes(files: List<FileItem>): List<FileItem> {
        return withContext(Dispatchers.IO) {
            files.map { file ->
                try {
                    val hash = HashingUtils.computeSha256(contentResolver, android.net.Uri.parse(file.uri))
                    file.copy(sha256 = hash)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to compute hash for file: ${file.displayName}")
                    file
                }
            }
        }
    }

    override suspend fun detectDuplicates(): List<DuplicateGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val duplicateGroupInfos = fileDao.getDuplicateGroups()
                
                // This is a simplified implementation
                // In a real app, you'd properly collect the flow and group files
                emptyList<DuplicateGroup>()
            } catch (e: Exception) {
                Timber.e(e, "Failed to detect duplicates")
                emptyList()
            }
        }
    }

    private fun FileEntity.toDomainModel(): FileItem {
        return FileItem(
            id = id,
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            size = size,
            bucket = bucket,
            dateModified = Date(dateModified),
            sha256 = sha256,
            mediaType = mediaType,
            isDuplicate = isDuplicate,
            groupHash = groupHash
        )
    }

    private fun FileItem.toEntity(): FileEntity {
        return FileEntity(
            id = id,
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            size = size,
            bucket = bucket,
            dateModified = dateModified.time,
            sha256 = sha256,
            mediaType = mediaType,
            isDuplicate = isDuplicate,
            groupHash = groupHash
        )
    }
}
