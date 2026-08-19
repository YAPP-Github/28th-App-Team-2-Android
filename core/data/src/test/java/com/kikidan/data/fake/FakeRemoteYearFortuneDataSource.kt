package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteYearFortuneDataSource
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune

class FakeRemoteYearFortuneDataSource : RemoteYearFortuneDataSource {
    var yearFortune: YearFortune =
        YearFortune(
            id = "f-1",
            year = 2026,
            score = 85,
            title = "title",
            content = "content",
            categories = listOf(FortuneCategoryStar(FortuneCategory.MONEY, 3)),
        )
    var throwOnPostYearFortune: Throwable? = null
    var throwOnGetYearFortune: Throwable? = null

    override suspend fun postYearFortune(year: Int): YearFortune {
        throwOnPostYearFortune?.let { throw it }
        return yearFortune
    }

    override suspend fun getYearFortune(id: String): YearFortune {
        throwOnGetYearFortune?.let { throw it }
        return yearFortune
    }
}
