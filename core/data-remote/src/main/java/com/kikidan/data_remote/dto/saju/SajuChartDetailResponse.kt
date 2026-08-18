package com.kikidan.data_remote.dto.saju

import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar
import kotlinx.serialization.Serializable

@Serializable
data class SajuChartDetailResponse(
    val pillars: List<PillarResponse>,
)

@Serializable
data class PillarResponse(
    val pillarType: String,
    val heavenlyStem: StemResponse,
    val earthlyBranch: BranchResponse,
)

@Serializable
data class StemResponse(
    val code: String,
)

@Serializable
data class BranchResponse(
    val code: String,
)

internal fun SajuChartDetailResponse.toDomain(): SajuPalja {
    fun pillarOf(type: String) = pillars.firstOrNull { it.pillarType.equals(type, ignoreCase = true) }
    val year = requireNotNull(pillarOf("YEAR")) { "year pillar missing in response" }
    val month = requireNotNull(pillarOf("MONTH")) { "month pillar missing in response" }
    val day = requireNotNull(pillarOf("DAY")) { "day pillar missing in response" }
    val hour = pillarOf("HOUR")
    return SajuPalja(
        yearPillar = year.toSajuPillar(),
        monthPillar = month.toSajuPillar(),
        dayPillar = day.toSajuPillar(),
        hourPillar = hour?.toSajuPillar(),
    )
}

private fun PillarResponse.toSajuPillar(): SajuPillar =
    SajuPillar(cheonGan = heavenlyStem.code.toCheonGan(), jiJi = earthlyBranch.code.toJiJi())
