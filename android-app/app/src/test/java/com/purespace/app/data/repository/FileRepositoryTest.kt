package com.purespace.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purespace.app.data.local.database.PureSpaceDatabase
import com.purespace.app.data.local.entity.FileEntity
import com.purespace.app.domain.model.FileItem
import com.purespace.app.domain.model.FileType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FileRepositoryTest {

    private lateinit var database: PureSpaceDatabase
    private lateinit var repository: FileRepositoryImpl

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PureSpaceDatabase::class.java
        ).build()
        repository = FileRepositoryImpl(database.fileDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetFile() = runTest {
        val fileItem = FileItem(
            id = "test-id",
            name = "test.jpg",
            path = "/storage/test.jpg",
            size = 1024L,
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "test-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        repository.insertFile(fileItem)
        val files = repository.getAllFiles().first()

        assertEquals(1, files.size)
        assertEquals(fileItem.name, files[0].name)
        assertEquals(fileItem.path, files[0].path)
        assertEquals(fileItem.size, files[0].size)
    }

    @Test
    fun insertMultipleFilesAndGetByType() = runTest {
        val imageFile = FileItem(
            id = "image-id",
            name = "image.jpg",
            path = "/storage/image.jpg",
            size = 2048L,
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "image-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        val videoFile = FileItem(
            id = "video-id",
            name = "video.mp4",
            path = "/storage/video.mp4",
            size = 10485760L,
            mimeType = "video/mp4",
            type = FileType.VIDEO,
            hash = "video-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        repository.insertFiles(listOf(imageFile, videoFile))

        val imageFiles = repository.getFilesByType(FileType.IMAGE).first()
        val videoFiles = repository.getFilesByType(FileType.VIDEO).first()

        assertEquals(1, imageFiles.size)
        assertEquals(1, videoFiles.size)
        assertEquals("image.jpg", imageFiles[0].name)
        assertEquals("video.mp4", videoFiles[0].name)
    }

    @Test
    fun markFileAsDeleted() = runTest {
        val fileItem = FileItem(
            id = "delete-test-id",
            name = "delete-test.jpg",
            path = "/storage/delete-test.jpg",
            size = 1024L,
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "delete-test-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        repository.insertFile(fileItem)
        repository.markFilesAsDeleted(listOf("delete-test-id"))

        val files = repository.getAllFiles().first()
        assertEquals(1, files.size)
        assertTrue(files[0].isDeleted)
    }

    @Test
    fun getDuplicatesByHash() = runTest {
        val file1 = FileItem(
            id = "dup1",
            name = "duplicate1.jpg",
            path = "/storage/duplicate1.jpg",
            size = 1024L,
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "same-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        val file2 = FileItem(
            id = "dup2",
            name = "duplicate2.jpg",
            path = "/storage/duplicate2.jpg",
            size = 1024L,
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "same-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        val file3 = FileItem(
            id = "unique",
            name = "unique.jpg",
            path = "/storage/unique.jpg",
            size = 2048L,
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "unique-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        repository.insertFiles(listOf(file1, file2, file3))

        val duplicates = repository.getDuplicateGroups().first()
        assertEquals(1, duplicates.size)
        assertEquals("same-hash", duplicates[0].hash)
        assertEquals(2, duplicates[0].count)
        assertEquals(2048L, duplicates[0].totalSize) // 1024 * 2
    }

    @Test
    fun getLargeFiles() = runTest {
        val smallFile = FileItem(
            id = "small",
            name = "small.jpg",
            path = "/storage/small.jpg",
            size = 1024L, // 1KB
            mimeType = "image/jpeg",
            type = FileType.IMAGE,
            hash = "small-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        val largeFile = FileItem(
            id = "large",
            name = "large.mp4",
            path = "/storage/large.mp4",
            size = 104857600L, // 100MB
            mimeType = "video/mp4",
            type = FileType.VIDEO,
            hash = "large-hash",
            lastModified = System.currentTimeMillis(),
            isDeleted = false
        )

        repository.insertFiles(listOf(smallFile, largeFile))

        val largeFiles = repository.getLargeFiles(50 * 1024 * 1024L).first() // 50MB threshold
        assertEquals(1, largeFiles.size)
        assertEquals("large.mp4", largeFiles[0].name)
    }
}
