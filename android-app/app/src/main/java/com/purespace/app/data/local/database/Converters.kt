package com.purespace.app.data.local.database

import androidx.room.TypeConverter
import com.purespace.app.domain.model.MediaType

class Converters {
    
    @TypeConverter
    fun fromMediaType(mediaType: MediaType): String {
        return mediaType.name
    }
    
    @TypeConverter
    fun toMediaType(mediaType: String): MediaType {
        return MediaType.valueOf(mediaType)
    }
}
