package com.kikidan.domain.repository

import com.kikidan.domain.model.fortune.YearFortune

interface YearFortuneRepository {
    suspend fun createYearFortune(year: Int): Result<YearFortune>

    suspend fun getYearFortune(id: String): Result<YearFortune>
}
