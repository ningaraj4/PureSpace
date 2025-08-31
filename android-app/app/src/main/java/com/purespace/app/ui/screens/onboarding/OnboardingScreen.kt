package com.purespace.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.purespace.app.R
import com.purespace.app.util.PermissionUtils

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = PermissionUtils.getAllRequiredPermissions().toList()
    )
    
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.onPermissionsGranted()
            onNavigateToDashboard()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon
        Image(
            painter = painterResource(id = R.drawable.ic_purespace_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Welcome Text
        Text(
            text = stringResource(R.string.welcome_to_purespace),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Features List
        OnboardingFeature(
            icon = R.drawable.ic_scan,
            title = stringResource(R.string.feature_smart_scan),
            description = stringResource(R.string.feature_smart_scan_desc)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OnboardingFeature(
            icon = R.drawable.ic_duplicate,
            title = stringResource(R.string.feature_duplicate_detection),
            description = stringResource(R.string.feature_duplicate_detection_desc)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OnboardingFeature(
            icon = R.drawable.ic_cleanup,
            title = stringResource(R.string.feature_safe_cleanup),
            description = stringResource(R.string.feature_safe_cleanup_desc)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Privacy Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.privacy_first),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.privacy_notice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission Request Button
        Button(
            onClick = {
                if (permissionsState.allPermissionsGranted) {
                    onNavigateToDashboard()
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (permissionsState.allPermissionsGranted) {
                    stringResource(R.string.get_started)
                } else {
                    stringResource(R.string.grant_permissions)
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        if (!permissionsState.allPermissionsGranted && permissionsState.shouldShowRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.permission_rationale),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OnboardingFeature(
    icon: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
