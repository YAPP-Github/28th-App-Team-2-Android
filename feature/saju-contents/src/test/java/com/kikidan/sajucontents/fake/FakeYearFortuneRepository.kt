package com.kikidan.sajucontents.fake

import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.repository.YearFortuneRepository

/**
 * core/domain에는 test-fixtures가 구성되어 있지 않아 동일한 Fake를 모듈별로 둔다.
 * `GetYearFortuneUseCase`가 final class라 이 Fake로 실제 UseCase를 조립해 사용한다.
 */
class FakeYearFortuneRepository : YearFortuneRepository {
    var result: Result<YearFortune> =
        Result.success(
            YearFortune(
                id = "fake-id",
                year = 2026,
                score = 0,
                title = "",
                content = "",
                categories = emptyList(),
            ),
        )
    var lastYear: Int? = null
        private set

    override suspend fun getYearFortune(year: Int): Result<YearFortune> {
        lastYear = year
        return result
    }
}
