package com.kikidan.sajucontents.fake

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.repository.DayFortuneRepository
import java.time.LocalDate

/**
 * core/domain에는 test-fixtures가 구성되어 있지 않아 동일한 Fake를 모듈별로 둔다.
 * `CreateDayFortunesUseCase`가 final class라 이 Fake로 실제 UseCase를 조립해 사용한다.
 */
class FakeDayFortuneRepository : DayFortuneRepository {
    var result: Result<List<DayFortune>> = Result.success(emptyList())
    var callCount: Int = 0
        private set
    var lastPurpose: DayFortunePurpose? = null
        private set
    var lastTargetDates: List<LocalDate>? = null
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
}
