package com.kikidan.data_remote.dto.saju

import com.kikidan.domain.model.saju.FiveElementCount
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.saju.SajuPillarType
import com.kikidan.domain.model.saju.TenGod
import kotlinx.serialization.Serializable

@Serializable
data class SajuChartDetailResponse(
    val dayMaster: StemResponse,
    val pillars: List<PillarResponse>,
    val ohaeng: List<OhaengResponse>,
)

@Serializable
data class PillarResponse(
    val pillarType: String,
    val heavenlyStem: StemResponse,
    val earthlyBranch: BranchResponse,
    val stemSipseong: SipseongResponse?,
    val branchSipseong: SipseongResponse,
    val jijanggan: List<StemResponse>,
    val sibiunseong: SibiunseongResponse,
    val sinsal: SinsalResponse,
)

@Serializable
data class StemResponse(
    val code: String,
)

@Serializable
data class BranchResponse(
    val code: String,
)

@Serializable
data class SipseongResponse(
    val code: String,
    val label: String,
)

@Serializable
data class SibiunseongResponse(
    val code: String,
    val label: String,
)

@Serializable
data class SinsalResponse(
    val code: String,
    val label: String,
)

@Serializable
data class ElementResponse(
    val code: String,
    val label: String? = null,
)

@Serializable
data class OhaengResponse(
    val element: ElementResponse,
    val count: Int,
    val percentage: Double,
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

internal fun SajuChartDetailResponse.toChartDetail(): SajuChartDetail =
    SajuChartDetail(
        dayMaster = dayMaster.code.toCheonGan(),
        pillars = pillars.map { it.toSajuPillarDetail() },
        fiveElements =
            ohaeng.map { entry ->
                FiveElementCount(
                    ohaeng = entry.element.code.toOhaeng(),
                    count = entry.count,
                    percentage = entry.percentage,
                )
            },
    )

private fun PillarResponse.toSajuPillar(): SajuPillar =
    SajuPillar(cheonGan = heavenlyStem.code.toCheonGan(), jiJi = earthlyBranch.code.toJiJi())

private fun PillarResponse.toSajuPillarDetail(): SajuPillarDetail =
    SajuPillarDetail(
        pillarType = SajuPillarType.valueOf(pillarType.uppercase()),
        cheonGan = heavenlyStem.code.toCheonGan(),
        jiJi = earthlyBranch.code.toJiJi(),
        stemTenGod = stemSipseong?.code?.toTenGod() ?: TenGod.ILWON,
        branchTenGod = branchSipseong.code.toTenGod(),
        hiddenStems = jijanggan.map { it.code.toCheonGan() },
        twelveUnseong = sibiunseong.code.toTwelveUnseong(),
        twelveSinsal = sinsal.code.toTwelveSinsal(),
    )
