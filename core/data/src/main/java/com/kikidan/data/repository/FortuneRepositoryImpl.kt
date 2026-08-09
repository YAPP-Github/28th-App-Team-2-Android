package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.repository.FortuneRepository
import com.kikidan.domain.util.runCatchingCancellable
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class FortuneRepositoryImpl
    @Inject
    constructor(
        private val remoteFortuneDataSource: RemoteFortuneDataSource,
    ) : FortuneRepository {
        // 같은 to로 다시 조회해도 네트워크 요청은 최초 1회뿐이다. 이 인스턴스 자체가 Hilt @ViewModelScoped라
        // ViewModel이 사라지면 캐시도 함께 사라진다. 동시 접근에 안전하도록 ConcurrentHashMap을 쓴다.
        private val historyCache = ConcurrentHashMap<LocalDate, List<DailyFortuneHistoryEntry>>()

        override suspend fun getTodayFortuneScores(): Result<List<FortuneScore>> =
            runCatchingCancellable { remoteFortuneDataSource.getTodayFortuneScores() }

        // 날짜별 luck-actions 단건 조회 API가 없어, history에서 해당 날짜의 dailyFortuneId를 찾은 뒤
        // detail 조회로 카테고리별 점수를 채운다. actions는 history 응답에 이미 포함되어 있다.
        // 해당 날짜에 기록이 없는 것은 에러가 아니라 정상적인 경계 도달이므로 null로 반환한다.
        override suspend fun getFortuneRecordForDate(date: LocalDate): Result<FortuneRecord?> =
            runCatchingCancellable {
                val entry =
                    allowedHistoryEntries()
                        .firstOrNull { it.fortuneDate == date }
                        ?: return@runCatchingCancellable null
                val scores = remoteFortuneDataSource.getFortuneDetailScores(entry.id)
                FortuneRecord(scores = scores, actions = entry.actions)
            }

        override suspend fun getEarliestFortuneDate(): Result<LocalDate?> =
            runCatchingCancellable { allowedHistoryEntries().minOfOrNull { it.fortuneDate } }

        // API가 허용하는 조회 범위(이번 달 1일~오늘, 지난달 전체)를 합쳐서 반환한다.
        private suspend fun allowedHistoryEntries(): List<DailyFortuneHistoryEntry> {
            val today = LocalDate.now()
            val lastMonthEnd = today.withDayOfMonth(1).minusDays(1)
            return (fortuneHistory(today) + fortuneHistory(lastMonthEnd)).distinctBy { it.fortuneDate }
        }

        private suspend fun fortuneHistory(to: LocalDate): List<DailyFortuneHistoryEntry> =
            historyCache[to] ?: remoteFortuneDataSource.getFortuneHistory(to).also { historyCache[to] = it }
    }
