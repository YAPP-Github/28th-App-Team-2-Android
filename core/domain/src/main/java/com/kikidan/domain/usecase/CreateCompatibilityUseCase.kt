package com.kikidan.domain.usecase

import com.kikidan.domain.model.compatibility.Compatibility
import com.kikidan.domain.repository.CompatibilityRepository
import javax.inject.Inject

class CreateCompatibilityUseCase
    @Inject
    constructor(
        private val compatibilityRepository: CompatibilityRepository,
    ) {
        suspend operator fun invoke(partnerLinkId: String): Result<Compatibility> =
            compatibilityRepository.createCompatibility(partnerLinkId)
    }
