package com.kikidan.sajucontents.fake

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.saju.RelationshipType
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.repository.PartnerSajuRepository
import java.time.LocalDate

/**
 * core/domain에는 test-fixtures가 구성되어 있지 않아 동일한 Fake를 모듈별로 둔다.
 */
class FakePartnerSajuRepository : PartnerSajuRepository {
    var listResult: Result<List<PartnerSaju>> =
        Result.success(
            listOf(
                PartnerSaju(
                    linkId = "partner-1",
                    name = "토실이",
                    gender = Gender.MALE,
                    relationshipType = RelationshipType("LOVER", "연인"),
                    birth = Birth(DateType.SOLAR, LocalDate.of(2001, 5, 30), BirthTime.O),
                ),
            ),
        )
    var getResult: Result<PartnerSaju> = listResult.map { it.first() }
    var chartDetailResult: Result<SajuChartDetail> =
        Result.success(SajuChartDetail(dayMaster = CheonGan.GI, pillars = emptyList(), fiveElements = emptyList()))
    var registerResult: Result<Unit> = Result.success(Unit)
    var lastRegisterInput: PartnerSajuInput? = null

    override suspend fun getPartnerSajuList(): Result<List<PartnerSaju>> = listResult

    override suspend fun getPartnerSaju(linkId: String): Result<PartnerSaju> = getResult

    override suspend fun getPartnerSajuChartDetail(linkId: String): Result<SajuChartDetail> = chartDetailResult

    override suspend fun deletePartnerSaju(linkId: String): Result<Unit> = Result.success(Unit)

    override suspend fun registerPartnerSaju(input: PartnerSajuInput): Result<Unit> {
        lastRegisterInput = input
        return registerResult
    }

    override suspend fun updatePartnerSaju(
        linkId: String,
        input: PartnerSajuInput,
    ): Result<Unit> = Result.success(Unit)
}
