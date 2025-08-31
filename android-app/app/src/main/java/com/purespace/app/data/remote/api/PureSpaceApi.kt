package com.purespace.app.data.remote.api

import com.purespace.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PureSpaceApi {
    
    // Authentication endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
    
    @GET("profile")
    suspend fun getProfile(): Response<UserDto>
    
    // File operations
    @POST("files/metadata")
    suspend fun uploadMetadata(@Body request: UploadMetadataRequest): Response<Unit>
    
    @GET("files")
    suspend fun getFiles(): Response<FilesResponse>
    
    @GET("files/stats")
    suspend fun getStats(): Response<StatsDto>
    
    // Duplicate detection
    @GET("duplicates/groups")
    suspend fun getDuplicateGroups(
        @Query("limit") limit: Int? = null,
        @Query("include_files") includeFiles: Boolean = false
    ): Response<DuplicateGroupsResponse>
    
    @GET("duplicates/groups/{sha256}/files")
    suspend fun getDuplicateGroupFiles(@Path("sha256") sha256: String): Response<DuplicateFilesResponse>
    
    @DELETE("duplicates/files")
    suspend fun deleteDuplicateFiles(@Body request: DeleteDuplicatesRequest): Response<Unit>
    
    @GET("duplicates/analyze")
    suspend fun analyzeDuplicates(): Response<DuplicateAnalysisDto>
    
    // Advanced duplicate detection
    @GET("duplicates/detect")
    suspend fun detectDuplicatesAdvanced(
        @Query("strategy") strategy: String = "hash"
    ): Response<AdvancedDuplicateResponse>
    
    @GET("duplicates/clusters/{cluster_id}")
    suspend fun getDuplicateCluster(@Path("cluster_id") clusterId: String): Response<DuplicateClusterDto>
    
    @GET("duplicates/compare-strategies")
    suspend fun compareDuplicateStrategies(): Response<StrategyComparisonResponse>
    
    // Large files
    @GET("large-files")
    suspend fun getLargeFiles(
        @Query("min_size") minSize: Long? = null,
        @Query("limit") limit: Int? = null
    ): Response<LargeFilesResponse>
}
