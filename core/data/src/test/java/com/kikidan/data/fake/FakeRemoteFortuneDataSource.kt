package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.TodayFortune
import java.time.LocalDate

class FakeRemoteFortuneDataSource : RemoteFortuneDataSource {
    var todayFortune: TodayFortune? = null
    var throwOnGetTodayFortune: Throwable? = null

    var history: List<DailyFortuneHistoryEntry> = emptyList()
    var throwOnGetFortuneHistory: Throwable? = null
    val requestedToValues: MutableList<LocalDate> = mutableListOf()
    val lastRequestedTo: LocalDate? get() = requestedToValues.lastOrNull()

    var detailScores: List<FortuneScore> = emptyList()
    var throwOnGetFortuneDetailScores: Throwable? = null
    var lastRequestedDailyFortuneId: String? = null

    var dailyFortuneDetail: DailyFortuneDetail? = null
    var throwOnGetDailyFortuneDetail: Throwable? = null
    var lastRequestedDailyFortuneDetailId: String? = null

    override suspend fun getTodayFortune(): TodayFortune {
        throwOnGetTodayFortune?.let { throw it }
        return todayFortune!!
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

    override suspend fun getDailyFortuneDetail(dailyFortuneId: String): DailyFortuneDetail {
        lastRequestedDailyFortuneDetailId = dailyFortuneId
        throwOnGetDailyFortuneDetail?.let { throw it }
        return dailyFortuneDetail!!
    }
}
