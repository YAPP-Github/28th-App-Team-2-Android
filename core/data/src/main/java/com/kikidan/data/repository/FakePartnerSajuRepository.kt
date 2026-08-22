package com.kikidan.data.repository

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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePartnerSajuRepository
    @Inject
    constructor() : PartnerSajuRepository {
        private var partners = MockPartnerSajuList

        override suspend fun getPartnerSajuList(): Result<List<PartnerSaju>> = Result.success(partners)

        override suspend fun getPartnerSaju(linkId: String): Result<PartnerSaju> {
            val partner = partners.find { it.linkId == linkId }
            return if (partner != null) {
                Result.success(partner)
            } else {
                Result.failure(NoSuchElementException("PartnerSaju not found: $linkId"))
            }
        }

        override suspend fun getPartnerSajuChartDetail(linkId: String): Result<SajuChartDetail> =
            Result.success(MockSajuChartDetail)

        override suspend fun deletePartnerSaju(linkId: String): Result<Unit> {
            partners = partners.filterNot { it.linkId == linkId }
            return Result.success(Unit)
        }

        override suspend fun registerPartnerSaju(input: PartnerSajuInput): Result<Unit> {
            partners = partners + input.toPartnerSaju(linkId = UUID.randomUUID().toString())
            return Result.success(Unit)
        }

        override suspend fun updatePartnerSaju(
            linkId: String,
            input: PartnerSajuInput,
        ): Result<Unit> {
            partners = partners.map { if (it.linkId == linkId) input.toPartnerSaju(linkId) else it }
            return Result.success(Unit)
        }
    }

private fun PartnerSajuInput.toPartnerSaju(linkId: String): PartnerSaju =
    PartnerSaju(
        linkId = linkId,
        name = name,
        gender = gender,
        relationshipType = MockRelationshipTypes.getValue(relationshipTypeCode),
        birth = birth,
    )

private val MockRelationshipTypes =
    mapOf(
        "LOVER" to RelationshipType(code = "LOVER", label = "연인"),
        "FRIEND" to RelationshipType(code = "FRIEND", label = "친구"),
        "COLLEAGUE" to RelationshipType(code = "COLLEAGUE", label = "동료"),
    )

private val MockSajuChartDetail =
    SajuChartDetail(
        dayMaster = CheonGan.GI,
        pillars = emptyList(),
        fiveElements = emptyList(),
    )

private val MockPartnerSajuList =
    listOf(
        PartnerSaju(
            linkId = UUID.randomUUID().toString(),
            name = "토실이",
            gender = Gender.MALE,
            relationshipType = RelationshipType(code = "LOVER", label = "연인"),
            birth = Birth(dateType = DateType.SOLAR, date = LocalDate.of(1999, 2, 13), time = BirthTime.O),
        ),
        PartnerSaju(
            linkId = UUID.randomUUID().toString(),
            name = "토실이",
            gender = Gender.MALE,
            relationshipType = RelationshipType(code = "COLLEAGUE", label = "동료"),
            birth = Birth(dateType = DateType.SOLAR, date = LocalDate.of(1999, 2, 13), time = BirthTime.O),
        ),
        PartnerSaju(
            linkId = UUID.randomUUID().toString(),
            name = "토실이",
            gender = Gender.MALE,
            relationshipType = RelationshipType(code = "FRIEND", label = "친구"),
            birth = Birth(dateType = DateType.SOLAR, date = LocalDate.of(1999, 2, 13), time = BirthTime.O),
        ),
    )
