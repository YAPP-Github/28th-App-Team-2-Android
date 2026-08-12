package com.kikidan.domain.repository

import com.kikidan.domain.model.fortune.YearFortune

interface YearFortuneRepository {
    suspend fun getYearFortune(year: Int): Result<YearFortune>
}
