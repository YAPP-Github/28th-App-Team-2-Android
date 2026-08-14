package com.kikidan.domain.usecase.saju

import com.kikidan.domain.di.Fake
import com.kikidan.domain.repository.PartnerSajuRepository
import javax.inject.Inject

class DeletePartnerSajuUseCase
    @Inject
    constructor(
        @Fake private val partnerSajuRepository: PartnerSajuRepository,
    ) {
        suspend operator fun invoke(linkId: String): Result<Unit> = partnerSajuRepository.deletePartnerSaju(linkId)
    }
