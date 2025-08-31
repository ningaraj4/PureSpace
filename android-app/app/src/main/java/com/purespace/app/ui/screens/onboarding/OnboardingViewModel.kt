package com.purespace.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    
    fun onPermissionsGranted() {
        viewModelScope.launch {
            Timber.d("Permissions granted, completing onboarding")
            // Mark onboarding as complete in preferences if needed
        }
    }
}
