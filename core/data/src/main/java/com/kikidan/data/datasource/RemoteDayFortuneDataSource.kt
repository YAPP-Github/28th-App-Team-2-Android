package com.kikidan.data.datasource

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import java.time.LocalDate

interface RemoteDayFortuneDataSource {
    suspend fun postDayFortunes(
        purpose: DayFortunePurpose,
        targetDates: List<LocalDate>,
    ): List<DayFortune>
}
