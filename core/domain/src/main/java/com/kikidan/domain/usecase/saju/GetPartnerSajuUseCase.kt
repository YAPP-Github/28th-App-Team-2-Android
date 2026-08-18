package com.kikidan.domain.usecase.saju

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.repository.PartnerSajuRepository
import javax.inject.Inject

class GetPartnerSajuUseCase
    @Inject
    constructor(
        private val partnerSajuRepository: PartnerSajuRepository,
    ) {
        suspend operator fun invoke(linkId: String): Result<PartnerSaju> = partnerSajuRepository.getPartnerSaju(linkId)
    }
