package com.kikidan.domain.usecase.saju

import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.repository.PartnerSajuRepository
import javax.inject.Inject

class GetPartnerSajuChartDetailUseCase
    @Inject
    constructor(
        private val partnerSajuRepository: PartnerSajuRepository,
    ) {
        suspend operator fun invoke(linkId: String): Result<SajuChartDetail> =
            partnerSajuRepository.getPartnerSajuChartDetail(linkId)
    }
