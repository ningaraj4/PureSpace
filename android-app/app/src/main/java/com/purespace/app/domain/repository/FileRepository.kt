package com.purespace.app.domain.repository

import com.purespace.app.domain.model.FileItem
import com.purespace.app.domain.model.DuplicateGroup
import com.purespace.app.domain.model.MediaType
import com.purespace.app.domain.model.Stats
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    
    fun getAllFiles(): Flow<List<FileItem>>
    
    fun getFilesByType(mediaType: MediaType): Flow<List<FileItem>>
    
    fun getLargeFiles(minSize: Long): Flow<List<FileItem>>
    
    fun getDuplicateGroups(): Flow<List<DuplicateGroup>>
    
    suspend fun insertFiles(files: List<FileItem>)
    
    suspend fun updateFile(file: FileItem)
    
    suspend fun markFilesAsDeleted(fileIds: List<String>)
    
    suspend fun getStats(): Stats
    
    suspend fun scanDeviceFiles(): List<FileItem>
    
    suspend fun computeHashes(files: List<FileItem>): List<FileItem>
    
    suspend fun detectDuplicates(): List<DuplicateGroup>
}
