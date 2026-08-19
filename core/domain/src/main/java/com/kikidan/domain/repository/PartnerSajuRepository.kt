package com.kikidan.domain.repository

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.saju.SajuChartDetail

interface PartnerSajuRepository {
    suspend fun getPartnerSajuList(): Result<List<PartnerSaju>>

    suspend fun getPartnerSaju(linkId: String): Result<PartnerSaju>

    suspend fun getPartnerSajuChartDetail(linkId: String): Result<SajuChartDetail>

    suspend fun deletePartnerSaju(linkId: String): Result<Unit>

    suspend fun registerPartnerSaju(input: PartnerSajuInput): Result<Unit>

    suspend fun updatePartnerSaju(
        linkId: String,
        input: PartnerSajuInput,
    ): Result<Unit>
}
