package com.kikidan.domain.usecase.saju

import com.kikidan.domain.di.Fake
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.repository.PartnerSajuRepository
import javax.inject.Inject

class GetPartnerSajuListUseCase
    @Inject
    constructor(
        @Fake private val partnerSajuRepository: PartnerSajuRepository,
    ) {
        suspend operator fun invoke(): Result<List<PartnerSaju>> = partnerSajuRepository.getPartnerSajuList()
    }
