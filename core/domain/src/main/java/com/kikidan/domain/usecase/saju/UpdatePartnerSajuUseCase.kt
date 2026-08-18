package com.kikidan.domain.usecase.saju

import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.repository.PartnerSajuRepository
import javax.inject.Inject

class UpdatePartnerSajuUseCase
    @Inject
    constructor(
        private val partnerSajuRepository: PartnerSajuRepository,
    ) {
        suspend operator fun invoke(
            linkId: String,
            input: PartnerSajuInput,
        ): Result<Unit> = partnerSajuRepository.updatePartnerSaju(linkId, input)
    }
