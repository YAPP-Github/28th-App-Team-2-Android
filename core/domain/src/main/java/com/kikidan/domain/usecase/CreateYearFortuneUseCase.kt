package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.repository.YearFortuneRepository
import javax.inject.Inject

class CreateYearFortuneUseCase
    @Inject
    constructor(
        private val yearFortuneRepository: YearFortuneRepository,
    ) {
        suspend operator fun invoke(year: Int): Result<YearFortune> = yearFortuneRepository.createYearFortune(year)
    }
