package com.purespace.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    // BillingManager is already provided via @Inject constructor
    // No additional providers needed
}
