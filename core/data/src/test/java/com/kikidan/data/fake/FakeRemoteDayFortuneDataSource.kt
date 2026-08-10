package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteDayFortuneDataSource
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import java.time.LocalDate

class FakeRemoteDayFortuneDataSource : RemoteDayFortuneDataSource {
    var dayFortunes: List<DayFortune> = emptyList()
    var throwOnPost: Throwable? = null

    override suspend fun postDayFortunes(
        purpose: DayFortunePurpose,
        targetDates: List<LocalDate>,
    ): List<DayFortune> {
        throwOnPost?.let { throw it }
        return dayFortunes
    }
}
