package com.kikidan.domain.usecase

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.repository.DayFortuneRepository
import javax.inject.Inject

class GetDayFortuneUseCase
    @Inject
    constructor(
        private val dayFortuneRepository: DayFortuneRepository,
    ) {
        suspend operator fun invoke(id: String): Result<DayFortune> = dayFortuneRepository.getDayFortune(id)
    }
