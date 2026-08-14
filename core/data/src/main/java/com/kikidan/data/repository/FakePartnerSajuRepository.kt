package com.kikidan.data.repository

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.RelationshipType
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

        override suspend fun deletePartnerSaju(linkId: String): Result<Unit> {
            partners = partners.filterNot { it.linkId == linkId }
            return Result.success(Unit)
        }
    }

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
