package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.repository.FortuneRepository
import com.kikidan.domain.util.runCatchingCancellable
import java.time.LocalDate
import javax.inject.Inject

class FortuneRepositoryImpl
    @Inject
    constructor(
        private val remoteFortuneDataSource: RemoteFortuneDataSource,
    ) : FortuneRepository {
        override suspend fun getTodayFortuneScores(): Result<List<FortuneScore>> =
            runCatchingCancellable { remoteFortuneDataSource.getTodayFortuneScores() }

        // 날짜별 luck-actions 단건 조회 API가 없어, history에서 해당 날짜의 dailyFortuneId를 찾은 뒤
        // detail 조회로 카테고리별 점수를 채운다. actions는 history 응답에 이미 포함되어 있다.
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
            // 오늘의 액션은 전용 API 사용, 오전 6시의 배치처리 한계
            val yesterday = LocalDate.now().minusDays(1)
            val lastMonthEnd = yesterday.withDayOfMonth(1).minusDays(1)
            val thisMonth = remoteFortuneDataSource.getFortuneHistory(yesterday)
            val lastMonth = remoteFortuneDataSource.getFortuneHistory(lastMonthEnd)
            return (thisMonth + lastMonth).distinctBy { it.fortuneDate }
        }
    }
