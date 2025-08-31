package com.purespace.app.domain.usecase

import com.purespace.app.domain.model.Stats
import com.purespace.app.domain.repository.FileRepository
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(): Stats {
        return fileRepository.getStats()
    }
}
