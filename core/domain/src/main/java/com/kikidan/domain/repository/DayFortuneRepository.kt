package com.kikidan.domain.repository

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import java.time.LocalDate

interface DayFortuneRepository {
    suspend fun createDayFortunes(
        purpose: DayFortunePurpose,
        targetDates: List<LocalDate>,
    ): Result<List<DayFortune>>
}
