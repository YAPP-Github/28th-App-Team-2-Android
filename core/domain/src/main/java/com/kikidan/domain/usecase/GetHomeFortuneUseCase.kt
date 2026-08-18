package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.TodayFortune
import com.kikidan.domain.repository.FortuneRepository
import javax.inject.Inject

class GetHomeFortuneUseCase
    @Inject
    constructor(
        private val fortuneRepository: FortuneRepository,
    ) {
        suspend operator fun invoke(): Result<TodayFortune> = fortuneRepository.getTodayFortune()
    }
