package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteSajuDataSource
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.repository.SajuRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class SajuRepositoryImpl
    @Inject
    constructor(
        private val remoteSajuDataSource: RemoteSajuDataSource,
    ) : SajuRepository {
        override suspend fun getSajuPalja(): Result<SajuPalja> =
            runCatchingCancellable { remoteSajuDataSource.getMySajuPalja() }

        override suspend fun getMySajuChartDetail(): Result<SajuChartDetail> =
            runCatchingCancellable { remoteSajuDataSource.getMySajuChartDetail() }
    }
