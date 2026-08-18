package com.kikidan.domain.usecase.saju

import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.repository.SajuRepository
import javax.inject.Inject

class GetSajuChartDetailUseCase
    @Inject
    constructor(
        private val sajuRepository: SajuRepository,
    ) {
        suspend operator fun invoke(): Result<SajuChartDetail> = sajuRepository.getMySajuChartDetail()
    }
