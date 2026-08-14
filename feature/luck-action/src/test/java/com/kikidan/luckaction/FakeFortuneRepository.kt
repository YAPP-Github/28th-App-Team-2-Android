package com.kikidan.luckaction

import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.TodayFortune
import com.kikidan.domain.repository.FortuneRepository
import java.time.LocalDate

class FakeFortuneRepository : FortuneRepository {
    var todayFortuneResult: Result<TodayFortune> = Result.failure(NotImplementedError())
    var recordResult: Result<FortuneRecord?> = Result.failure(NotImplementedError())
    var earliestDateResult: Result<LocalDate?> = Result.success(null)

    override suspend fun getTodayFortune(): Result<TodayFortune> = todayFortuneResult

    override suspend fun getFortuneRecordForDate(date: LocalDate): Result<FortuneRecord?> = recordResult

    override suspend fun getEarliestFortuneDate(): Result<LocalDate?> = earliestDateResult
}
