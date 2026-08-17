package com.kikidan.data.datasource

import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneScore
import java.time.LocalDate

interface RemoteFortuneDataSource {
    suspend fun getTodayFortuneScores(): List<FortuneScore>

    suspend fun getFortuneHistory(to: LocalDate): List<DailyFortuneHistoryEntry>

    suspend fun getFortuneDetailScores(dailyFortuneId: String): List<FortuneScore>
}
