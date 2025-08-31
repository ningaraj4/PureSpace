package com.purespace.app.di

import android.content.ContentResolver
import android.content.Context
import com.purespace.app.data.local.dao.FileDao
import com.purespace.app.data.repository.FileRepositoryImpl
import com.purespace.app.domain.repository.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        fileDao: FileDao,
        contentResolver: ContentResolver
    ): FileRepository {
        return FileRepositoryImpl(fileDao, contentResolver)
    }
}
