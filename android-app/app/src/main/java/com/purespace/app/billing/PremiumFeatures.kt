package com.purespace.app.billing

object PremiumFeatures {
    
    // Feature limits for free users
    const val FREE_DUPLICATE_SCAN_LIMIT = 100
    const val FREE_LARGE_FILE_SCAN_LIMIT = 50
    const val FREE_CLOUD_SYNC_DEVICES = 1
    const val FREE_ADVANCED_DETECTION_LIMIT = 10
    
    // Premium feature flags
    enum class Feature {
        UNLIMITED_DUPLICATE_DETECTION,
        UNLIMITED_LARGE_FILE_DETECTION,
        ADVANCED_DUPLICATE_ALGORITHMS,
        CLOUD_SYNC_MULTIPLE_DEVICES,
        SCHEDULED_AUTOMATIC_SCANS,
        PRIORITY_CUSTOMER_SUPPORT,
        AD_FREE_EXPERIENCE,
        EXPORT_REPORTS,
        CUSTOM_SCAN_FILTERS,
        BULK_OPERATIONS
    }
    
    // Premium benefits
    data class PremiumBenefit(
        val feature: Feature,
        val title: String,
        val description: String,
        val icon: String
    )
    
    val premiumBenefits = listOf(
        PremiumBenefit(
            feature = Feature.UNLIMITED_DUPLICATE_DETECTION,
            title = "Unlimited Duplicate Detection",
            description = "Scan and find all duplicates without limits",
            icon = "🔍"
        ),
        PremiumBenefit(
            feature = Feature.ADVANCED_DUPLICATE_ALGORITHMS,
            title = "Advanced Detection Algorithms",
            description = "AI-powered similarity detection and smart clustering",
            icon = "🧠"
        ),
        PremiumBenefit(
            feature = Feature.CLOUD_SYNC_MULTIPLE_DEVICES,
            title = "Multi-Device Cloud Sync",
            description = "Sync your data across unlimited devices",
            icon = "☁️"
        ),
        PremiumBenefit(
            feature = Feature.SCHEDULED_AUTOMATIC_SCANS,
            title = "Automatic Scheduled Scans",
            description = "Set up daily, weekly, or monthly automatic scans",
            icon = "⏰"
        ),
        PremiumBenefit(
            feature = Feature.EXPORT_REPORTS,
            title = "Export Detailed Reports",
            description = "Export scan results and cleanup reports",
            icon = "📊"
        ),
        PremiumBenefit(
            feature = Feature.PRIORITY_CUSTOMER_SUPPORT,
            title = "Priority Support",
            description = "Get priority customer support and assistance",
            icon = "🎧"
        ),
        PremiumBenefit(
            feature = Feature.AD_FREE_EXPERIENCE,
            title = "Ad-Free Experience",
            description = "Enjoy PureSpace without any advertisements",
            icon = "🚫"
        ),
        PremiumBenefit(
            feature = Feature.BULK_OPERATIONS,
            title = "Bulk Operations",
            description = "Delete, move, or manage files in bulk",
            icon = "⚡"
        )
    )
    
    // Check if a feature is available for the user
    fun isFeatureAvailable(feature: Feature, hasPremium: Boolean): Boolean {
        return when (feature) {
            Feature.AD_FREE_EXPERIENCE,
            Feature.UNLIMITED_DUPLICATE_DETECTION,
            Feature.UNLIMITED_LARGE_FILE_DETECTION,
            Feature.ADVANCED_DUPLICATE_ALGORITHMS,
            Feature.CLOUD_SYNC_MULTIPLE_DEVICES,
            Feature.SCHEDULED_AUTOMATIC_SCANS,
            Feature.PRIORITY_CUSTOMER_SUPPORT,
            Feature.EXPORT_REPORTS,
            Feature.CUSTOM_SCAN_FILTERS,
            Feature.BULK_OPERATIONS -> hasPremium
        }
    }
    
    // Get usage limit for a feature
    fun getUsageLimit(feature: Feature, hasPremium: Boolean): Int? {
        if (hasPremium) return null // No limits for premium users
        
        return when (feature) {
            Feature.UNLIMITED_DUPLICATE_DETECTION -> FREE_DUPLICATE_SCAN_LIMIT
            Feature.UNLIMITED_LARGE_FILE_DETECTION -> FREE_LARGE_FILE_SCAN_LIMIT
            Feature.ADVANCED_DUPLICATE_ALGORITHMS -> FREE_ADVANCED_DETECTION_LIMIT
            Feature.CLOUD_SYNC_MULTIPLE_DEVICES -> FREE_CLOUD_SYNC_DEVICES
            else -> null
        }
    }
    
    // Check if user has reached usage limit
    fun hasReachedLimit(feature: Feature, currentUsage: Int, hasPremium: Boolean): Boolean {
        val limit = getUsageLimit(feature, hasPremium) ?: return false
        return currentUsage >= limit
    }
}
