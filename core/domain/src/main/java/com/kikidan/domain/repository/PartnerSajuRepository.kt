package com.kikidan.domain.repository

import com.kikidan.domain.model.saju.PartnerSaju

interface PartnerSajuRepository {
    suspend fun getPartnerSajuList(): Result<List<PartnerSaju>>

    suspend fun deletePartnerSaju(linkId: String): Result<Unit>
}
