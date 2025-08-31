package com.purespace.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purespace.app.data.auth.AuthManager
import com.purespace.app.data.auth.AuthState
import com.purespace.app.data.auth.GoogleSignInManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val googleSignInManager: GoogleSignInManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Monitor auth state changes
        viewModelScope.launch {
            authManager.authState.collect { authState ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = authState is AuthState.Authenticated,
                    isLoading = authState is AuthState.Loading
                )
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Get Google ID token
                val idTokenResult = googleSignInManager.signIn()
                
                if (idTokenResult.isSuccess) {
                    val idToken = idTokenResult.getOrThrow()
                    
                    // Authenticate with backend
                    val authResult = authManager.signInWithGoogle(idToken)
                    
                    if (authResult.isSuccess) {
                        Timber.d("Authentication successful")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            errorMessage = null
                        )
                    } else {
                        val error = authResult.exceptionOrNull()
                        Timber.e(error, "Backend authentication failed")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Authentication failed: ${error?.message}"
                        )
                    }
                } else {
                    val error = idTokenResult.exceptionOrNull()
                    Timber.e(error, "Google Sign-In failed")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Google Sign-In failed: ${error?.message}"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error during authentication")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
)
