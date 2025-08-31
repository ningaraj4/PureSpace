package com.purespace.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "id_token")
    val idToken: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val email: String,
    val provider: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class UploadMetadataRequest(
    @Json(name = "device_id")
    val deviceId: String,
    val files: List<FileItemDto>
)

@JsonClass(generateAdapter = true)
data class FileItemDto(
    val sha256: String,
    val size: Long,
    val mime: String?,
    @Json(name = "path_tail")
    val pathTail: String
)

@JsonClass(generateAdapter = true)
data class FilesResponse(
    val files: List<FileDto>
)

@JsonClass(generateAdapter = true)
data class FileDto(
    val id: Int,
    @Json(name = "user_id")
    val userId: String,
    @Json(name = "device_id")
    val deviceId: String,
    @Json(name = "path_tail")
    val pathTail: String,
    val mime: String?,
    val size: Long,
    val sha256: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class StatsDto(
    @Json(name = "total_files")
    val totalFiles: Int,
    @Json(name = "duplicate_bytes")
    val duplicateBytes: Long,
    @Json(name = "potential_savings")
    val potentialSavings: Long
)

@JsonClass(generateAdapter = true)
data class DuplicateGroupsResponse(
    val groups: List<DuplicateGroupDto>
)

@JsonClass(generateAdapter = true)
data class DuplicateGroupDto(
    val sha256: String,
    val count: Int,
    @Json(name = "total_size")
    val totalSize: Long,
    val files: List<FileDto>? = null
)

@JsonClass(generateAdapter = true)
data class DuplicateFilesResponse(
    val files: List<FileDto>
)

@JsonClass(generateAdapter = true)
data class DeleteDuplicatesRequest(
    @Json(name = "file_ids")
    val fileIds: List<Int>
)

@JsonClass(generateAdapter = true)
data class DuplicateAnalysisDto(
    @Json(name = "total_groups")
    val totalGroups: Int,
    @Json(name = "total_duplicates")
    val totalDuplicates: Int,
    @Json(name = "potential_savings")
    val potentialSavings: Long,
    @Json(name = "largest_group")
    val largestGroup: DuplicateGroupDto?
)

@JsonClass(generateAdapter = true)
data class AdvancedDuplicateResponse(
    val strategy: String,
    val summary: DuplicateSummaryDto,
    val clusters: List<DuplicateClusterDto>
)

@JsonClass(generateAdapter = true)
data class DuplicateSummaryDto(
    @Json(name = "total_clusters")
    val totalClusters: Int,
    @Json(name = "total_duplicates")
    val totalDuplicates: Int,
    @Json(name = "potential_savings")
    val potentialSavings: Long
)

@JsonClass(generateAdapter = true)
data class DuplicateClusterDto(
    val id: String,
    val sha256: String?,
    val size: Long,
    val count: Int,
    @Json(name = "total_size")
    val totalSize: Long,
    val candidates: List<DuplicateCandidateDto>,
    val strategy: Int,
    @Json(name = "created_at")
    val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class DuplicateCandidateDto(
    val file: FileDto,
    val confidence: Double,
    val reason: String
)

@JsonClass(generateAdapter = true)
data class StrategyComparisonResponse(
    val comparison: Map<String, StrategyResultDto>,
    val recommendation: String
)

@JsonClass(generateAdapter = true)
data class StrategyResultDto(
    @Json(name = "total_clusters")
    val totalClusters: Int?,
    @Json(name = "total_duplicates")
    val totalDuplicates: Int?,
    @Json(name = "potential_savings")
    val potentialSavings: Long?,
    @Json(name = "avg_confidence")
    val avgConfidence: Double?,
    val error: String?
)

@JsonClass(generateAdapter = true)
data class LargeFilesResponse(
    val files: List<FileDto>,
    @Json(name = "min_size")
    val minSize: Long
)
