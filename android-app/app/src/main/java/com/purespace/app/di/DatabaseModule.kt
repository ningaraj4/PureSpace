package com.purespace.app.di

import android.content.Context
import androidx.room.Room
import com.purespace.app.data.local.dao.FileDao
import com.purespace.app.data.local.dao.ScanSessionDao
import com.purespace.app.data.local.database.PureSpaceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PureSpaceDatabase {
        return Room.databaseBuilder(
            context,
            PureSpaceDatabase::class.java,
            PureSpaceDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFileDao(database: PureSpaceDatabase): FileDao {
        return database.fileDao()
    }

    @Provides
    fun provideScanSessionDao(database: PureSpaceDatabase): ScanSessionDao {
        return database.scanSessionDao()
    }
}
