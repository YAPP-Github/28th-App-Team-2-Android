package com.kikidan.domain.usecase.saju

import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.repository.PartnerSajuRepository
import javax.inject.Inject

class RegisterPartnerSajuUseCase
    @Inject
    constructor(
        private val partnerSajuRepository: PartnerSajuRepository,
    ) {
        suspend operator fun invoke(input: PartnerSajuInput): Result<Unit> =
            partnerSajuRepository.registerPartnerSaju(input)
    }
