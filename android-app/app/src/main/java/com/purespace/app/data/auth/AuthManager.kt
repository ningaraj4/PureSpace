package com.purespace.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.purespace.app.data.local.preferences.PreferencesManager
import com.purespace.app.data.remote.api.PureSpaceApi
import com.purespace.app.data.remote.dto.LoginRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val preferencesManager: PreferencesManager,
    private val api: PureSpaceApi
) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: Flow<AuthState> = _authState.asStateFlow()
    
    val isAuthenticated: Flow<Boolean> = authState.map { it is AuthState.Authenticated }
    
    init {
        // Initialize auth state based on current user and stored JWT
        updateAuthState()
        
        // Listen to Firebase auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            updateAuthState()
        }
    }
    
    private fun updateAuthState() {
        val firebaseUser = firebaseAuth.currentUser
        val jwtToken = preferencesManager.getJwtToken()
        
        _authState.value = when {
            firebaseUser != null && jwtToken != null -> {
                AuthState.Authenticated(firebaseUser, jwtToken)
            }
            firebaseUser != null && jwtToken == null -> {
                AuthState.FirebaseOnly(firebaseUser)
            }
            else -> AuthState.Unauthenticated
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            // Sign in with Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase user is null")
            
            // Get Firebase ID token for backend authentication
            val firebaseIdToken = firebaseUser.getIdToken(false).await().token
                ?: throw Exception("Failed to get Firebase ID token")
            
            // Authenticate with backend
            val loginRequest = LoginRequest(idToken = firebaseIdToken)
            val response = api.login(loginRequest)
            
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                
                // Store JWT token and user info
                preferencesManager.saveJwtToken(loginResponse.token)
                preferencesManager.saveUserInfo(
                    userId = loginResponse.user.id,
                    email = loginResponse.user.email,
                    provider = loginResponse.user.provider
                )
                
                updateAuthState()
                Timber.d("Authentication successful for user: ${loginResponse.user.email}")
                Result.success(Unit)
            } else {
                Timber.e("Backend authentication failed: ${response.code()}")
                Result.failure(Exception("Backend authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Authentication failed")
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            // Sign out from Firebase
            firebaseAuth.signOut()
            
            // Clear stored tokens and user info
            preferencesManager.clearAuthData()
            
            // Notify backend about logout (optional)
            try {
                api.logout()
            } catch (e: Exception) {
                Timber.w(e, "Backend logout failed, but continuing with local logout")
            }
            
            updateAuthState()
            Timber.d("User signed out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sign out failed")
            Result.failure(e)
        }
    }
    
    suspend fun refreshToken(): Result<String> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No Firebase user"))
            
            // Get fresh Firebase ID token
            val firebaseIdToken = firebaseUser.getIdToken(true).await().token
                ?: return Result.failure(Exception("Failed to get Firebase ID token"))
            
            // Re-authenticate with backend
            val loginRequest = LoginRequest(idToken = firebaseIdToken)
            val response = api.login(loginRequest)
            
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                preferencesManager.saveJwtToken(loginResponse.token)
                updateAuthState()
                Result.success(loginResponse.token)
            } else {
                Result.failure(Exception("Token refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed")
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
    
    fun getJwtToken(): String? = preferencesManager.getJwtToken()
    
    fun getUserId(): String? = preferencesManager.getUserId()
    
    fun getUserEmail(): String? = preferencesManager.getUserEmail()
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class FirebaseOnly(val firebaseUser: FirebaseUser) : AuthState()
    data class Authenticated(val firebaseUser: FirebaseUser, val jwtToken: String) : AuthState()
}
