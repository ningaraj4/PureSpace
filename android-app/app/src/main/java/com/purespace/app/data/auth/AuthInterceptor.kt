package com.purespace.app.data.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for login endpoint
        if (originalRequest.url.encodedPath.contains("/auth/login")) {
            return chain.proceed(originalRequest)
        }
        
        // Add JWT token to request
        val jwtToken = authManager.getJwtToken()
        val request = if (jwtToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $jwtToken")
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(request)
        
        // Handle 401 Unauthorized - try to refresh token
        if (response.code == 401 && jwtToken != null) {
            response.close()
            
            return try {
                // Attempt token refresh
                val refreshResult = runBlocking { authManager.refreshToken() }
                
                if (refreshResult.isSuccess) {
                    val newToken = refreshResult.getOrNull()
                    val newRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                    
                    Timber.d("Token refreshed, retrying request")
                    chain.proceed(newRequest)
                } else {
                    Timber.w("Token refresh failed, returning 401")
                    // Return original 401 response
                    chain.proceed(request)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during token refresh")
                chain.proceed(request)
            }
        }
        
        return response
    }
}
