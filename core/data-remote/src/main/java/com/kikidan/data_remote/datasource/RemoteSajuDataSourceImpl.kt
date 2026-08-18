package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteSajuDataSource
import com.kikidan.data_remote.dto.saju.SajuChartDetailResponse
import com.kikidan.data_remote.dto.saju.toChartDetail
import com.kikidan.data_remote.dto.saju.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class RemoteSajuDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteSajuDataSource {
        override suspend fun getMySajuPalja(): SajuPalja =
            client.get().get(SAJU_ME_URL).bodyNotNull<SajuChartDetailResponse>().toDomain()

        override suspend fun getMySajuChartDetail(): SajuChartDetail =
            client.get().get(SAJU_ME_URL).bodyNotNull<SajuChartDetailResponse>().toChartDetail()

        companion object {
            private const val SAJU_ME_URL = "api/v1/saju/me"
        }
    }
