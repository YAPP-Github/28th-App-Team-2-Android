package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteYearFortuneDataSource
import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.repository.YearFortuneRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class YearFortuneRepositoryImpl
    @Inject
    constructor(
        private val remoteFortuneDataSource: RemoteYearFortuneDataSource,
    ) : YearFortuneRepository {
        override suspend fun createYearFortune(year: Int): Result<YearFortune> =
            runCatchingCancellable {
                remoteFortuneDataSource.postYearFortune(year)
            }

        override suspend fun getYearFortune(id: String): Result<YearFortune> =
            runCatchingCancellable {
                remoteFortuneDataSource.getYearFortune(id)
            }
    }
