package com.purespace.app.data.repository

import com.purespace.app.data.remote.api.PureSpaceApi
import com.purespace.app.data.remote.dto.*
import com.purespace.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFileRepository @Inject constructor(
    private val api: PureSpaceApi
) {

    suspend fun uploadMetadata(deviceId: String, files: List<FileItem>): Result<Unit> {
        return try {
            val fileItems = files.map { file ->
                FileItemDto(
                    sha256 = file.hash,
                    size = file.size,
                    mime = file.mimeType,
                    pathTail = file.path.substringAfterLast("/")
                )
            }
            
            val request = UploadMetadataRequest(
                deviceId = deviceId,
                files = fileItems
            )
            
            val response = api.uploadMetadata(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<Stats> {
        return try {
            val response = api.getStats()
            if (response.isSuccessful) {
                val statsDto = response.body()!!
                val stats = Stats(
                    totalFiles = statsDto.totalFiles,
                    totalSize = 0L, // Not provided by backend
                    duplicateFiles = 0, // Calculate from duplicate bytes
                    duplicateSize = statsDto.duplicateBytes,
                    largeFiles = 0, // Not provided
                    largeFilesSize = 0L, // Not provided
                    potentialSavings = statsDto.potentialSavings
                )
                Result.success(stats)
            } else {
                Result.failure(Exception("Failed to get stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDuplicateGroups(): Flow<List<DuplicateGroup>> = flow {
        try {
            val response = api.getDuplicateGroups(includeFiles = true)
            if (response.isSuccessful) {
                val groups = response.body()?.groups?.map { dto ->
                    DuplicateGroup(
                        id = dto.sha256,
                        files = dto.files?.map { fileDto ->
                            FileItem(
                                id = fileDto.id.toLong(),
                                path = fileDto.pathTail,
                                name = fileDto.pathTail.substringAfterLast("/"),
                                size = fileDto.size,
                                mimeType = fileDto.mime ?: "",
                                hash = fileDto.sha256,
                                mediaType = MediaType.fromMimeType(fileDto.mime ?: ""),
                                dateModified = System.currentTimeMillis(), // Backend doesn't provide this
                                isSelected = false
                            )
                        } ?: emptyList(),
                        totalSize = dto.totalSize,
                        duplicateCount = dto.count
                    )
                } ?: emptyList()
                emit(groups)
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getAdvancedDuplicates(strategy: DetectionStrategy): Flow<List<AdvancedDuplicateCluster>> = flow {
        try {
            val strategyParam = when (strategy) {
                DetectionStrategy.HASH -> "hash"
                DetectionStrategy.SIZE -> "size"
                DetectionStrategy.SIZE_AND_NAME -> "size_name"
                DetectionStrategy.ADVANCED -> "advanced"
            }
            
            val response = api.detectDuplicatesAdvanced(strategyParam)
            if (response.isSuccessful) {
                val clusters = response.body()?.clusters?.map { dto ->
                    AdvancedDuplicateCluster(
                        id = dto.id,
                        sha256 = dto.sha256,
                        size = dto.size,
                        count = dto.count,
                        totalSize = dto.totalSize,
                        strategy = strategy,
                        candidates = dto.candidates.map { candidate ->
                            DuplicateCandidate(
                                file = FileItem(
                                    id = candidate.file.id.toLong(),
                                    path = candidate.file.pathTail,
                                    name = candidate.file.pathTail.substringAfterLast("/"),
                                    size = candidate.file.size,
                                    mimeType = candidate.file.mime ?: "",
                                    hash = candidate.file.sha256,
                                    mediaType = MediaType.fromMimeType(candidate.file.mime ?: ""),
                                    dateModified = System.currentTimeMillis(),
                                    isSelected = false
                                ),
                                confidence = candidate.confidence,
                                reason = candidate.reason
                            )
                        }
                    )
                } ?: emptyList()
                emit(clusters)
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun deleteDuplicateFiles(fileIds: List<Int>): Result<Unit> {
        return try {
            val request = DeleteDuplicatesRequest(fileIds)
            val response = api.deleteDuplicateFiles(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLargeFiles(minSize: Long, limit: Int): Result<List<FileItem>> {
        return try {
            val response = api.getLargeFiles(minSize, limit)
            if (response.isSuccessful) {
                val files = response.body()?.files?.map { dto ->
                    FileItem(
                        id = dto.id.toLong(),
                        path = dto.pathTail,
                        name = dto.pathTail.substringAfterLast("/"),
                        size = dto.size,
                        mimeType = dto.mime ?: "",
                        hash = dto.sha256,
                        mediaType = MediaType.fromMimeType(dto.mime ?: ""),
                        dateModified = System.currentTimeMillis(),
                        isSelected = false
                    )
                } ?: emptyList()
                Result.success(files)
            } else {
                Result.failure(Exception("Failed to get large files: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compareStrategies(): Result<StrategyComparison> {
        return try {
            val response = api.compareDuplicateStrategies()
            if (response.isSuccessful) {
                val body = response.body()!!
                val comparison = StrategyComparison(
                    strategies = body.comparison.mapValues { (_, result) ->
                        StrategyResult(
                            totalClusters = result.totalClusters ?: 0,
                            totalDuplicates = result.totalDuplicates ?: 0,
                            potentialSavings = result.potentialSavings ?: 0L,
                            avgConfidence = result.avgConfidence ?: 0.0,
                            error = result.error
                        )
                    },
                    recommendation = body.recommendation
                )
                Result.success(comparison)
            } else {
                Result.failure(Exception("Failed to compare strategies: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Domain models for advanced duplicate detection
enum class DetectionStrategy {
    HASH, SIZE, SIZE_AND_NAME, ADVANCED
}

data class AdvancedDuplicateCluster(
    val id: String,
    val sha256: String?,
    val size: Long,
    val count: Int,
    val totalSize: Long,
    val strategy: DetectionStrategy,
    val candidates: List<DuplicateCandidate>
)

data class DuplicateCandidate(
    val file: FileItem,
    val confidence: Double,
    val reason: String
)

data class StrategyComparison(
    val strategies: Map<String, StrategyResult>,
    val recommendation: String
)

data class StrategyResult(
    val totalClusters: Int,
    val totalDuplicates: Int,
    val potentialSavings: Long,
    val avgConfidence: Double,
    val error: String?
)
