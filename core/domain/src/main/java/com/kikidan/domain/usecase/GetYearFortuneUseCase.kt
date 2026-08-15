package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.repository.YearFortuneRepository
import javax.inject.Inject

class GetYearFortuneUseCase
    @Inject
    constructor(
        private val yearFortuneRepository: YearFortuneRepository,
    ) {
        suspend operator fun invoke(id: String): Result<YearFortune> = yearFortuneRepository.getYearFortune(id)
    }
