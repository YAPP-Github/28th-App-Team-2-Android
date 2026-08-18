package com.kikidan.data.datasource

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.PartnerSajuInput

interface RemotePartnerSajuDataSource {
    suspend fun getPartnerSajuList(): List<PartnerSaju>

    suspend fun getPartnerSaju(linkId: String): PartnerSaju

    suspend fun deletePartnerSaju(linkId: String)

    suspend fun registerPartnerSaju(input: PartnerSajuInput)

    suspend fun updatePartnerSaju(
        linkId: String,
        input: PartnerSajuInput,
    )
}
