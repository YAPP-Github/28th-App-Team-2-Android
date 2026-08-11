package com.kikidan.domain.fake

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.repository.DayFortuneRepository
import java.time.LocalDate

class FakeDayFortuneRepository : DayFortuneRepository {
    var result: Result<List<DayFortune>> = Result.success(emptyList())
    var callCount: Int = 0
        private set
    var lastPurpose: DayFortunePurpose? = null
        private set
    var lastTargetDates: List<LocalDate>? = null
        private set

    var getResult: Result<DayFortune> = Result.failure(IllegalStateException("설정되지 않았습니다."))
    var lastGetId: String? = null
        private set

    override suspend fun createDayFortunes(
        purpose: DayFortunePurpose,
        targetDates: List<LocalDate>,
    ): Result<List<DayFortune>> {
        callCount++
        lastPurpose = purpose
        lastTargetDates = targetDates
        return result
    }

    override suspend fun getDayFortune(id: String): Result<DayFortune> {
        lastGetId = id
        return getResult
    }
}
