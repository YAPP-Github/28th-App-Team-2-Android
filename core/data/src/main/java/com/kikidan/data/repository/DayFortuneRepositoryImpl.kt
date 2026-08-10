package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteDayFortuneDataSource
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.repository.DayFortuneRepository
import com.kikidan.domain.util.runCatchingCancellable
import java.time.LocalDate
import javax.inject.Inject

class DayFortuneRepositoryImpl
    @Inject
    constructor(
        private val remoteDayFortuneDataSource: RemoteDayFortuneDataSource,
    ) : DayFortuneRepository {
        override suspend fun createDayFortunes(
            purpose: DayFortunePurpose,
            targetDates: List<LocalDate>,
        ): Result<List<DayFortune>> =
            runCatchingCancellable {
                remoteDayFortuneDataSource.postDayFortunes(purpose, targetDates)
            }
    }
