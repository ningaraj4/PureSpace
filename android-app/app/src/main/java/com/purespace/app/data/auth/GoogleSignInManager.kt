package com.purespace.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val credentialManager = CredentialManager.create(context)
    
    companion object {
        // Replace with your actual web client ID from Firebase Console
        private const val WEB_CLIENT_ID = "your-web-client-id.googleusercontent.com"
    }
    
    suspend fun signIn(): Result<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = credential.idToken
            
            Timber.d("Google Sign-In successful")
            Result.success(idToken)
            
        } catch (e: GetCredentialException) {
            Timber.e(e, "Google Sign-In failed")
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Timber.e(e, "Google ID token parsing failed")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during Google Sign-In")
            Result.failure(e)
        }
    }
}
