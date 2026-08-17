package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.repository.FortuneRepository
import javax.inject.Inject

class GetDailyFortuneDetailUseCase
    @Inject
    constructor(
        private val fortuneRepository: FortuneRepository,
    ) {
        suspend operator fun invoke(dailyFortuneId: String): Result<DailyFortuneDetail> =
            fortuneRepository.getDailyFortuneDetail(dailyFortuneId)
    }
