package com.purespace.app.domain.usecase

import com.purespace.app.domain.model.FileItem
import com.purespace.app.domain.repository.FileRepository
import javax.inject.Inject

class ScanDeviceMediaUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(): List<FileItem> {
        return fileRepository.scanDeviceFiles()
    }
}
