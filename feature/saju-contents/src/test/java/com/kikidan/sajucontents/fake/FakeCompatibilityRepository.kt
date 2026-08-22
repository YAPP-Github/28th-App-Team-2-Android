package com.kikidan.sajucontents.fake

import com.kikidan.domain.model.compatibility.Compatibility
import com.kikidan.domain.model.compatibility.CompatibilityOhaeng
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.RelationshipType
import com.kikidan.domain.repository.CompatibilityRepository

/**
 * core/domain에는 test-fixtures가 구성되어 있지 않아 동일한 Fake를 모듈별로 둔다.
 */
class FakeCompatibilityRepository : CompatibilityRepository {
    var result: Result<Compatibility> =
        Result.success(
            Compatibility(
                id = "compat-1",
                partnerName = "토실이",
                relationshipType = RelationshipType("LOVER", "연인"),
                score = 85,
                headline = "함께할 수록 빛나는 궁합",
                subheadline = "함께 있을 때, 편안함이 커지는 사이예요.",
                summary = "요약",
                totalAnalysis = "총운 분석",
                analysisBasis = "사주 팔자 기반",
                ohaengs = listOf(CompatibilityOhaeng(Ohaeng.MOK, 25)),
            ),
        )
    var lastPartnerLinkId: String? = null
    var lastCompatibilityId: String? = null

    override suspend fun createCompatibility(partnerLinkId: String): Result<Compatibility> {
        lastPartnerLinkId = partnerLinkId
        return result
    }

    override suspend fun getCompatibility(compatibilityId: String): Result<Compatibility> {
        lastCompatibilityId = compatibilityId
        return result
    }
}
