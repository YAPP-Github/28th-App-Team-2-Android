package com.kikidan.domain.repository

import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.TodayFortune
import java.time.LocalDate

interface FortuneRepository {
    suspend fun getTodayFortune(): Result<TodayFortune>

    // 해당 날짜에 기록이 없으면 (에러가 아니라) success(null)을 반환한다.
    suspend fun getFortuneRecordForDate(date: LocalDate): Result<FortuneRecord?>

    // 조회 가능한 범위(이번 달 + 지난달) 중 기록이 존재하는 가장 오래된 날짜. 기록이 전혀 없으면 null.
    suspend fun getEarliestFortuneDate(): Result<LocalDate?>

    suspend fun getDailyFortuneDetail(dailyFortuneId: String): Result<DailyFortuneDetail>
}
