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
        // DataSource가 (to) 기준으로 raw 응답을 캐싱하므로, 반복 호출해도 최초 2회 이후로는 네트워크 요청이 없다.
        private suspend fun allowedHistoryEntries(): List<DailyFortuneHistoryEntry> {
            val today = LocalDate.now()
            val lastMonthEnd = today.withDayOfMonth(1).minusDays(1)
            val thisMonth = remoteFortuneDataSource.getFortuneHistory(today)
            val lastMonth = remoteFortuneDataSource.getFortuneHistory(lastMonthEnd)
            return (thisMonth + lastMonth).distinctBy { it.fortuneDate }
        }
    }
