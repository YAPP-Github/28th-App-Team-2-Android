package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneScore
import java.time.LocalDate

class FakeRemoteFortuneDataSource : RemoteFortuneDataSource {
    var scores: List<FortuneScore> = emptyList()
    var throwOnGetTodayFortuneScores: Throwable? = null

    var history: List<DailyFortuneHistoryEntry> = emptyList()
    var throwOnGetFortuneHistory: Throwable? = null
    val requestedToValues: MutableList<LocalDate> = mutableListOf()
    val lastRequestedTo: LocalDate? get() = requestedToValues.lastOrNull()

    var detailScores: List<FortuneScore> = emptyList()
    var throwOnGetFortuneDetailScores: Throwable? = null
    var lastRequestedDailyFortuneId: String? = null

    override suspend fun getTodayFortuneScores(): List<FortuneScore> {
        throwOnGetTodayFortuneScores?.let { throw it }
        return scores
    }

    override suspend fun getFortuneHistory(to: LocalDate): List<DailyFortuneHistoryEntry> {
        requestedToValues += to
        throwOnGetFortuneHistory?.let { throw it }
        return history
    }

    override suspend fun getFortuneDetailScores(dailyFortuneId: String): List<FortuneScore> {
        lastRequestedDailyFortuneId = dailyFortuneId
        throwOnGetFortuneDetailScores?.let { throw it }
        return detailScores
    }
}
