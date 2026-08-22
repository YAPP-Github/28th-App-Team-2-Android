package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemotePartnerSajuDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.saju.PartnerSajuDetailResponse
import com.kikidan.data_remote.dto.saju.PartnerSajuSummaryResponse
import com.kikidan.data_remote.dto.saju.RegisterPartnerSajuResponse
import com.kikidan.data_remote.dto.saju.SajuChartDetailResponse
import com.kikidan.data_remote.dto.saju.toChartDetail
import com.kikidan.data_remote.dto.saju.toDomain
import com.kikidan.data_remote.dto.saju.toRegisterRequest
import com.kikidan.data_remote.dto.saju.toUpdateRequest
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.saju.SajuChartDetail
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class RemotePartnerSajuDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemotePartnerSajuDataSource {
        override suspend fun getPartnerSajuList(): List<PartnerSaju> =
            client
                .get()
                .get(PARTNERS_URL)
                .bodyNotNull<List<PartnerSajuSummaryResponse>>()
                .map { it.toDomain() }

        override suspend fun getPartnerSaju(linkId: String): PartnerSaju =
            client
                .get()
                .get(partnerUrl(linkId))
                .bodyNotNull<PartnerSajuDetailResponse>()
                .toDomain()

        override suspend fun getPartnerSajuChartDetail(linkId: String): SajuChartDetail =
            client
                .get()
                .get(partnerUrl(linkId))
                .bodyNotNull<SajuChartDetailResponse>()
                .toChartDetail()

        override suspend fun deletePartnerSaju(linkId: String) {
            client.get().delete(partnerUrl(linkId)).body<CommonResponse<Unit>>()
        }

        override suspend fun registerPartnerSaju(input: PartnerSajuInput) {
            client
                .get()
                .post(PARTNERS_URL) { setBody(input.toRegisterRequest()) }
                .bodyNotNull<RegisterPartnerSajuResponse>()
        }

        override suspend fun updatePartnerSaju(
            linkId: String,
            input: PartnerSajuInput,
        ) {
            client
                .get()
                .patch(partnerUrl(linkId)) { setBody(input.toUpdateRequest()) }
                .body<CommonResponse<Unit>>()
        }

        companion object {
            private const val PARTNERS_URL = "api/v1/saju/partners"

            private fun partnerUrl(linkId: String) = "$PARTNERS_URL/$linkId"
        }
    }
