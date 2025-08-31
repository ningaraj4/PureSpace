package com.purespace.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.purespace.app.data.local.dao.FileDao
import com.purespace.app.data.local.dao.ScanSessionDao
import com.purespace.app.data.local.entity.FileEntity
import com.purespace.app.data.local.entity.ScanSessionEntity
import com.purespace.app.data.local.entity.CleanupActionEntity

@Database(
    entities = [
        FileEntity::class,
        ScanSessionEntity::class,
        CleanupActionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PureSpaceDatabase : RoomDatabase() {
    
    abstract fun fileDao(): FileDao
    abstract fun scanSessionDao(): ScanSessionDao
    
    companion object {
        const val DATABASE_NAME = "purespace_database"
        
        @Volatile
        private var INSTANCE: PureSpaceDatabase? = null
        
        fun getDatabase(context: Context): PureSpaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PureSpaceDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
