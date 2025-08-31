package com.purespace.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.purespace.app.billing.PremiumFeatures

@Composable
fun PremiumGate(
    feature: PremiumFeatures.Feature,
    hasPremium: Boolean,
    onUpgradeClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (PremiumFeatures.isFeatureAvailable(feature, hasPremium)) {
        content()
    } else {
        PremiumLockedContent(
            feature = feature,
            onUpgradeClick = onUpgradeClick
        )
    }
}

@Composable
fun PremiumUsageGate(
    feature: PremiumFeatures.Feature,
    currentUsage: Int,
    hasPremium: Boolean,
    onUpgradeClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!PremiumFeatures.hasReachedLimit(feature, currentUsage, hasPremium)) {
        content()
    } else {
        PremiumLimitReachedContent(
            feature = feature,
            currentUsage = currentUsage,
            onUpgradeClick = onUpgradeClick
        )
    }
}

@Composable
private fun PremiumLockedContent(
    feature: PremiumFeatures.Feature,
    onUpgradeClick: () -> Unit
) {
    val benefit = PremiumFeatures.premiumBenefits.find { it.feature == feature }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Premium Feature",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = benefit?.title ?: "Premium Feature",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = benefit?.description ?: "This feature requires a premium subscription.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Upgrade",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upgrade to Premium",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumLimitReachedContent(
    feature: PremiumFeatures.Feature,
    currentUsage: Int,
    onUpgradeClick: () -> Unit
) {
    val limit = PremiumFeatures.getUsageLimit(feature, false) ?: 0
    val benefit = PremiumFeatures.premiumBenefits.find { it.feature == feature }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Limit Reached",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "Usage Limit Reached",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "You've used $currentUsage of $limit ${benefit?.title?.lowercase() ?: "items"}. Upgrade to premium for unlimited access.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LinearProgressIndicator(
                progress = (currentUsage.toFloat() / limit.toFloat()).coerceAtMost(1f),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error
            )
            
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Upgrade",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get Unlimited Access",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
