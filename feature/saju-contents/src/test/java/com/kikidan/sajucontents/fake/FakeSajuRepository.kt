package com.kikidan.sajucontents.fake

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.repository.SajuRepository

/**
 * core/domain에는 test-fixtures가 구성되어 있지 않아 동일한 Fake를 모듈별로 둔다.
 */
class FakeSajuRepository : SajuRepository {
    var paljaResult: Result<SajuPalja> =
        Result.success(
            SajuPalja(
                yearPillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                monthPillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                dayPillar = SajuPillar(CheonGan.GI, JiJi.SA),
                hourPillar = SajuPillar(CheonGan.SIN, JiJi.MI),
            ),
        )

    var chartDetailResult: Result<SajuChartDetail> =
        Result.success(SajuChartDetail(dayMaster = CheonGan.GI, pillars = emptyList(), fiveElements = emptyList()))

    override suspend fun getSajuPalja(): Result<SajuPalja> = paljaResult

    override suspend fun getMySajuChartDetail(): Result<SajuChartDetail> = chartDetailResult
}
