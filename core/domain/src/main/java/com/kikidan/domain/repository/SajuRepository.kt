package com.kikidan.domain.repository

import com.kikidan.domain.model.saju.SajuPalja

interface SajuRepository {
    suspend fun getSajuPalja(): Result<SajuPalja>
}
