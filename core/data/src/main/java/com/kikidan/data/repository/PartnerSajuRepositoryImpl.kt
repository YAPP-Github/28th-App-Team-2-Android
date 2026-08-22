package com.kikidan.data.repository

import com.kikidan.data.datasource.RemotePartnerSajuDataSource
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.repository.PartnerSajuRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class PartnerSajuRepositoryImpl
    @Inject
    constructor(
        private val remotePartnerSajuDataSource: RemotePartnerSajuDataSource,
    ) : PartnerSajuRepository {
        override suspend fun getPartnerSajuList(): Result<List<PartnerSaju>> =
            runCatchingCancellable { remotePartnerSajuDataSource.getPartnerSajuList() }

        override suspend fun getPartnerSaju(linkId: String): Result<PartnerSaju> =
            runCatchingCancellable { remotePartnerSajuDataSource.getPartnerSaju(linkId) }

        override suspend fun getPartnerSajuChartDetail(linkId: String): Result<SajuChartDetail> =
            runCatchingCancellable { remotePartnerSajuDataSource.getPartnerSajuChartDetail(linkId) }

        override suspend fun deletePartnerSaju(linkId: String): Result<Unit> =
            runCatchingCancellable { remotePartnerSajuDataSource.deletePartnerSaju(linkId) }

        override suspend fun registerPartnerSaju(input: PartnerSajuInput): Result<Unit> =
            runCatchingCancellable { remotePartnerSajuDataSource.registerPartnerSaju(input) }

        override suspend fun updatePartnerSaju(
            linkId: String,
            input: PartnerSajuInput,
        ): Result<Unit> = runCatchingCancellable { remotePartnerSajuDataSource.updatePartnerSaju(linkId, input) }
    }
