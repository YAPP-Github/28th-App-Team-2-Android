package com.kikidan.domain.fake

import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.repository.FortuneRepository
import java.time.LocalDate

class FakeFortuneRepository : FortuneRepository {
    var scoresResult: Result<List<FortuneScore>> = Result.failure(NotImplementedError())
    var recordResult: Result<FortuneRecord?> = Result.failure(NotImplementedError())
    var earliestDateResult: Result<LocalDate?> = Result.success(null)
    var lastRequestedDate: LocalDate? = null

    override suspend fun getTodayFortuneScores(): Result<List<FortuneScore>> = scoresResult

    override suspend fun getFortuneRecordForDate(date: LocalDate): Result<FortuneRecord?> {
        lastRequestedDate = date
        return recordResult
    }

    override suspend fun getEarliestFortuneDate(): Result<LocalDate?> = earliestDateResult
}
