package com.kikidan.domain.usecase

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.repository.DayFortuneRepository
import java.time.LocalDate
import javax.inject.Inject

object DateFortuneDefaults {
    const val MAX_TARGET_DATES = 5
}

class CreateDayFortunesUseCase
    @Inject
    constructor(
        private val dayFortuneRepository: DayFortuneRepository,
    ) {
        suspend operator fun invoke(
            purpose: DayFortunePurpose,
            targetDates: List<LocalDate>,
        ): Result<List<DayFortune>> {
            if (targetDates.isEmpty() || targetDates.size > DateFortuneDefaults.MAX_TARGET_DATES) {
                return Result.failure(
                    IllegalArgumentException(
                        "targetDates 개수는 1~${DateFortuneDefaults.MAX_TARGET_DATES}개여야 합니다. size=${targetDates.size}",
                    ),
                )
            }
            val today = LocalDate.now()
            if (targetDates.any { it.isBefore(today) }) {
                return Result.failure(IllegalArgumentException("과거 날짜는 선택할 수 없습니다."))
            }
            return dayFortuneRepository.createDayFortunes(purpose, targetDates)
        }
    }
