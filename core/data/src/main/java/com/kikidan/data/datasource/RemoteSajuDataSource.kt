package com.kikidan.data.datasource

import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja

interface RemoteSajuDataSource {
    suspend fun getMySajuPalja(): SajuPalja

    suspend fun getMySajuChartDetail(): SajuChartDetail
}
