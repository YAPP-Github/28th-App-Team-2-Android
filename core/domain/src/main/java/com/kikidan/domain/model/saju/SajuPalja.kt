package com.kikidan.domain.model.saju

data class SajuPillar(
    val cheonGan: CheonGan,
    val jiJi: JiJi,
)

data class SajuPalja(
    val yearPillar: SajuPillar,
    val monthPillar: SajuPillar,
    val dayPillar: SajuPillar,
    val hourPillar: SajuPillar?,
)