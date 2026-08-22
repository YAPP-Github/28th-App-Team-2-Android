package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.data_remote.dto.fortune.DailyFortuneHistoryResponse
import com.kikidan.data_remote.dto.fortune.DailyFortuneResponse
import com.kikidan.data_remote.dto.fortune.TodayFortuneResponse
import com.kikidan.data_remote.dto.fortune.toDetail
import com.kikidan.data_remote.dto.fortune.toDomain
import com.kikidan.data_remote.dto.fortune.toFortuneScores
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.TodayFortune
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.time.LocalDate
import javax.inject.Inject

class RemoteFortuneDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteFortuneDataSource {
        override suspend fun getTodayFortune(): TodayFortune =
            client
                .get()
                .get(TODAY_FORTUNE_URL)
                .bodyNotNull<TodayFortuneResponse>()
                .toDomain()

        override suspend fun getFortuneHistory(to: LocalDate): List<DailyFortuneHistoryEntry> =
            client
                .get()
                .get(HISTORY_URL) { parameter("to", to.toString()) }
                .bodyNotNull<List<DailyFortuneHistoryResponse>>()
                .map { it.toDomain() }

        override suspend fun getFortuneDetailScores(dailyFortuneId: String): List<FortuneScore> =
            client
                .get()
                .get(detailUrl(dailyFortuneId))
                .bodyNotNull<DailyFortuneResponse>()
                .toFortuneScores()

        override suspend fun getDailyFortuneDetail(dailyFortuneId: String): DailyFortuneDetail =
            client
                .get()
                .get(detailUrl(dailyFortuneId))
                .bodyNotNull<DailyFortuneResponse>()
                .toDetail()

        companion object {
            private const val DAILY_FORTUNES_URL = "api/v1/daily-fortunes"
            private const val TODAY_FORTUNE_URL = "$DAILY_FORTUNES_URL/today"
            private const val HISTORY_URL = "$DAILY_FORTUNES_URL/history"

            private fun detailUrl(dailyFortuneId: String) = "$DAILY_FORTUNES_URL/$dailyFortuneId"
        }
    }
