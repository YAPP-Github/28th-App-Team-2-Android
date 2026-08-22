package com.kikidan.domain.repository

import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja

interface SajuRepository {
    suspend fun getSajuPalja(): Result<SajuPalja>

    suspend fun getMySajuChartDetail(): Result<SajuChartDetail>
}
