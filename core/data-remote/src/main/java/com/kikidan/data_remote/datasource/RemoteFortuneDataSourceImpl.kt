package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.data_remote.dto.fortune.DailyFortuneHistoryResponse
import com.kikidan.data_remote.dto.fortune.DailyFortuneResponse
import com.kikidan.data_remote.dto.fortune.TodayFortuneResponse
import com.kikidan.data_remote.dto.fortune.toDomain
import com.kikidan.data_remote.dto.fortune.toFortuneScores
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneScore
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
        // 히스토리는 (to) 기준으로 raw 응답을 세션 동안 캐싱한다. 같은 to로 다시 조회해도 네트워크 요청은 최초 1회뿐이다.
        // ponytail: 싱글턴 내 단순 var 캐시라 동시 접근 시 드물게 중복 요청이 발생할 수 있음 — 문제가 되면 Mutex로 승격.
        private val historyCache = mutableMapOf<LocalDate, List<DailyFortuneHistoryResponse>>()

        override suspend fun getTodayFortuneScores(): List<FortuneScore> =
            client
                .get()
                .get(TODAY_FORTUNE_URL)
                .bodyNotNull<TodayFortuneResponse>()
                .toFortuneScores()

        override suspend fun getFortuneHistory(to: LocalDate): List<DailyFortuneHistoryEntry> {
            val cached = historyCache[to]
            val responses =
                cached ?: client
                    .get()
                    .get(HISTORY_URL) { parameter("to", to.toString()) }
                    .bodyNotNull<List<DailyFortuneHistoryResponse>>()
                    .also { historyCache[to] = it }
            return responses.map { it.toDomain() }
        }

        override suspend fun getFortuneDetailScores(dailyFortuneId: String): List<FortuneScore> =
            client
                .get()
                .get(detailUrl(dailyFortuneId))
                .bodyNotNull<DailyFortuneResponse>()
                .toFortuneScores()

        companion object {
            private const val DAILY_FORTUNES_URL = "api/v1/daily-fortunes"
            private const val TODAY_FORTUNE_URL = "$DAILY_FORTUNES_URL/today"
            private const val HISTORY_URL = "$DAILY_FORTUNES_URL/history"

            private fun detailUrl(dailyFortuneId: String) = "$DAILY_FORTUNES_URL/$dailyFortuneId"
        }
    }
