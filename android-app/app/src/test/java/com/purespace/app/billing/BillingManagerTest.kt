package com.purespace.app.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class BillingManagerTest {

    private lateinit var context: Context
    private lateinit var billingManager: BillingManager

    @Mock
    private lateinit var mockPurchase: Purchase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        billingManager = BillingManager(context)
    }

    @Test
    fun initialBillingState() = runTest {
        val initialState = billingManager.billingState.first()
        
        assertFalse(initialState.isConnected)
        assertFalse(initialState.hasPremium)
        assertTrue(initialState.availableProducts.isEmpty())
        assertTrue(initialState.activePurchases.isEmpty())
        assertNull(initialState.error)
    }

    @Test
    fun premiumFeatureAvailability() {
        // Test free user limitations
        assertFalse(PremiumFeatures.isFeatureAvailable(
            PremiumFeatures.Feature.UNLIMITED_DUPLICATE_DETECTION, 
            hasPremium = false
        ))
        
        assertFalse(PremiumFeatures.isFeatureAvailable(
            PremiumFeatures.Feature.ADVANCED_DUPLICATE_ALGORITHMS, 
            hasPremium = false
        ))
        
        // Test premium user access
        assertTrue(PremiumFeatures.isFeatureAvailable(
            PremiumFeatures.Feature.UNLIMITED_DUPLICATE_DETECTION, 
            hasPremium = true
        ))
        
        assertTrue(PremiumFeatures.isFeatureAvailable(
            PremiumFeatures.Feature.ADVANCED_DUPLICATE_ALGORITHMS, 
            hasPremium = true
        ))
    }

    @Test
    fun usageLimits() {
        // Test free user limits
        assertEquals(
            PremiumFeatures.FREE_DUPLICATE_SCAN_LIMIT,
            PremiumFeatures.getUsageLimit(
                PremiumFeatures.Feature.UNLIMITED_DUPLICATE_DETECTION,
                hasPremium = false
            )
        )
        
        // Test premium user (no limits)
        assertNull(PremiumFeatures.getUsageLimit(
            PremiumFeatures.Feature.UNLIMITED_DUPLICATE_DETECTION,
            hasPremium = true
        ))
    }

    @Test
    fun hasReachedLimitCheck() {
        val feature = PremiumFeatures.Feature.UNLIMITED_DUPLICATE_DETECTION
        
        // Free user under limit
        assertFalse(PremiumFeatures.hasReachedLimit(
            feature, 
            currentUsage = 50, 
            hasPremium = false
        ))
        
        // Free user at limit
        assertTrue(PremiumFeatures.hasReachedLimit(
            feature, 
            currentUsage = PremiumFeatures.FREE_DUPLICATE_SCAN_LIMIT, 
            hasPremium = false
        ))
        
        // Premium user never reaches limit
        assertFalse(PremiumFeatures.hasReachedLimit(
            feature, 
            currentUsage = 1000, 
            hasPremium = true
        ))
    }

    @Test
    fun premiumBenefitsStructure() {
        val benefits = PremiumFeatures.premiumBenefits
        
        assertTrue(benefits.isNotEmpty())
        
        // Check that all major features are included
        val featureTypes = benefits.map { it.feature }.toSet()
        assertTrue(featureTypes.contains(PremiumFeatures.Feature.UNLIMITED_DUPLICATE_DETECTION))
        assertTrue(featureTypes.contains(PremiumFeatures.Feature.ADVANCED_DUPLICATE_ALGORITHMS))
        assertTrue(featureTypes.contains(PremiumFeatures.Feature.CLOUD_SYNC_MULTIPLE_DEVICES))
        
        // Check that all benefits have required fields
        benefits.forEach { benefit ->
            assertNotNull(benefit.title)
            assertNotNull(benefit.description)
            assertNotNull(benefit.icon)
            assertTrue(benefit.title.isNotEmpty())
            assertTrue(benefit.description.isNotEmpty())
            assertTrue(benefit.icon.isNotEmpty())
        }
    }
}
