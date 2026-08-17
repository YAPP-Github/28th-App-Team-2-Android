package com.kikidan.domain.usecase

import com.kikidan.domain.repository.FortuneRepository
import java.time.LocalDate
import javax.inject.Inject

class GetEarliestFortuneDateUseCase
    @Inject
    constructor(
        private val fortuneRepository: FortuneRepository,
    ) {
        suspend operator fun invoke(): Result<LocalDate?> = fortuneRepository.getEarliestFortuneDate()
    }
