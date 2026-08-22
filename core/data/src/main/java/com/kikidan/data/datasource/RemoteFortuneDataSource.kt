package com.kikidan.data.datasource

import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.TodayFortune
import java.time.LocalDate

interface RemoteFortuneDataSource {
    suspend fun getTodayFortune(): TodayFortune

    suspend fun getFortuneHistory(to: LocalDate): List<DailyFortuneHistoryEntry>

    suspend fun getFortuneDetailScores(dailyFortuneId: String): List<FortuneScore>

    suspend fun getDailyFortuneDetail(dailyFortuneId: String): DailyFortuneDetail
}
