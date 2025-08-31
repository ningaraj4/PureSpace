package com.purespace.app.data.local.dao

import androidx.room.*
import com.purespace.app.data.local.entity.ScanSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanSessionDao {
    
    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<ScanSessionEntity>>
    
    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLastSession(): ScanSessionEntity?
    
    @Query("SELECT * FROM scan_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ScanSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity)
    
    @Update
    suspend fun updateSession(session: ScanSessionEntity)
    
    @Query("DELETE FROM scan_sessions WHERE startedAt < :timestamp")
    suspend fun deleteOldSessions(timestamp: Long)
}
