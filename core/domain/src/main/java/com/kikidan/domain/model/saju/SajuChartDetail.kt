package com.kikidan.domain.model.saju

enum class SajuPillarType {
    YEAR,
    MONTH,
    DAY,
    HOUR,
}

data class SajuPillarDetail(
    val pillarType: SajuPillarType,
    val cheonGan: CheonGan,
    val jiJi: JiJi,
    val stemTenGod: TenGod,
    val branchTenGod: TenGod,
    val hiddenStems: List<CheonGan>,
    val twelveUnseong: TwelveUnseong,
    val twelveSinsal: TwelveSinsal,
)

data class FiveElementCount(
    val ohaeng: Ohaeng,
    val count: Int,
    val percentage: Double,
)

data class SajuChartDetail(
    val dayMaster: CheonGan,
    val pillars: List<SajuPillarDetail>,
    val fiveElements: List<FiveElementCount>,
)
