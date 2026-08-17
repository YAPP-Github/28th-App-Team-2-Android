package com.kikidan.home

import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.TodayFortune
import com.kikidan.domain.repository.FortuneRepository
import java.time.LocalDate

class FakeFortuneRepository : FortuneRepository {
    var todayFortuneResult: Result<TodayFortune> = Result.failure(NotImplementedError())
    var recordResult: Result<FortuneRecord?> = Result.failure(NotImplementedError())
    var earliestDateResult: Result<LocalDate?> = Result.success(null)
    var dailyFortuneDetailResult: Result<DailyFortuneDetail> = Result.failure(NotImplementedError())
    var lastRequestedDailyFortuneId: String? = null

    override suspend fun getTodayFortune(): Result<TodayFortune> = todayFortuneResult

    override suspend fun getFortuneRecordForDate(date: LocalDate): Result<FortuneRecord?> = recordResult

    override suspend fun getEarliestFortuneDate(): Result<LocalDate?> = earliestDateResult

    override suspend fun getDailyFortuneDetail(dailyFortuneId: String): Result<DailyFortuneDetail> {
        lastRequestedDailyFortuneId = dailyFortuneId
        return dailyFortuneDetailResult
    }
}
