package com.purespace.app.domain.usecase

import com.purespace.app.domain.model.DuplicateGroup
import com.purespace.app.domain.repository.FileRepository
import javax.inject.Inject

class DetectDuplicatesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(): List<DuplicateGroup> {
        return fileRepository.detectDuplicates()
    }
}
