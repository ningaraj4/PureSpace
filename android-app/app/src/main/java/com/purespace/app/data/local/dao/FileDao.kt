package com.purespace.app.data.local.dao

import androidx.room.*
import com.purespace.app.data.local.entity.FileEntity
import com.purespace.app.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    
    @Query("SELECT * FROM files WHERE isDeleted = 0")
    fun getAllFiles(): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE isDeleted = 0 AND mediaType = :mediaType")
    fun getFilesByType(mediaType: MediaType): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE isDeleted = 0 AND size >= :minSize ORDER BY size DESC")
    fun getLargeFiles(minSize: Long): Flow<List<FileEntity>>
    
    @Query("""
        SELECT sha256, COUNT(*) as count, SUM(size) as totalSize 
        FROM files 
        WHERE sha256 IS NOT NULL AND isDeleted = 0 
        GROUP BY sha256 
        HAVING COUNT(*) > 1
    """)
    fun getDuplicateGroups(): Flow<List<DuplicateGroupInfo>>
    
    @Query("SELECT * FROM files WHERE sha256 = :hash AND isDeleted = 0")
    fun getFilesByHash(hash: String): Flow<List<FileEntity>>
    
    @Query("SELECT COUNT(*) FROM files WHERE isDeleted = 0")
    suspend fun getTotalFileCount(): Int
    
    @Query("SELECT SUM(size) FROM files WHERE isDeleted = 0")
    suspend fun getTotalSize(): Long?
    
    @Query("SELECT COUNT(*) FROM files WHERE isDuplicate = 1 AND isDeleted = 0")
    suspend fun getDuplicateFileCount(): Int
    
    @Query("SELECT SUM(size) FROM files WHERE isDuplicate = 1 AND isDeleted = 0")
    suspend fun getDuplicateSize(): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)
    
    @Update
    suspend fun updateFile(file: FileEntity)
    
    @Query("UPDATE files SET isDeleted = 1 WHERE id IN (:fileIds)")
    suspend fun markFilesAsDeleted(fileIds: List<String>)
    
    @Query("UPDATE files SET isDuplicate = :isDuplicate, groupHash = :groupHash WHERE sha256 = :hash")
    suspend fun updateDuplicateStatus(hash: String, isDuplicate: Boolean, groupHash: String?)
    
    @Query("DELETE FROM files WHERE isDeleted = 1 AND createdAt < :timestamp")
    suspend fun cleanupDeletedFiles(timestamp: Long)
    
    @Query("SELECT * FROM files WHERE sha256 IS NULL AND isDeleted = 0 LIMIT :limit")
    suspend fun getFilesWithoutHash(limit: Int): List<FileEntity>
}

data class DuplicateGroupInfo(
    val sha256: String,
    val count: Int,
    val totalSize: Long
)
