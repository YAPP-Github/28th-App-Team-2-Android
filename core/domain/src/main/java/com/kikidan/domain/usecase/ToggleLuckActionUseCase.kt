package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.repository.LuckActionRepository
import javax.inject.Inject

class ToggleLuckActionUseCase
    @Inject
    constructor(
        private val luckActionRepository: LuckActionRepository,
    ) {
        suspend operator fun invoke(luckActionId: String): Result<LuckAction> =
            luckActionRepository.toggleAchievement(luckActionId)
    }
