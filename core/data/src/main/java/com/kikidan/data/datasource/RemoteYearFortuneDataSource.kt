package com.kikidan.data.datasource

import com.kikidan.domain.model.fortune.YearFortune

interface RemoteYearFortuneDataSource {
    suspend fun postYearFortune(year: Int): YearFortune
}
