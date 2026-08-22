package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.LuckActionDetail
import com.kikidan.domain.repository.LuckActionRepository
import javax.inject.Inject

class GetLuckActionDetailUseCase
    @Inject
    constructor(
        private val luckActionRepository: LuckActionRepository,
    ) {
        suspend operator fun invoke(luckActionId: String): Result<LuckActionDetail> =
            luckActionRepository.getLuckActionDetail(luckActionId)
    }
