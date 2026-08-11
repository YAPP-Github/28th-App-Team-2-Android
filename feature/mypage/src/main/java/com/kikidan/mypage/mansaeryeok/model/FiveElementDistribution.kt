package com.kikidan.mypage.mansaeryeok.model

import com.kikidan.domain.model.saju.Ohaeng

data class FiveElementDistribution(
    val ohaeng: Ohaeng,
    val count: Int,
    val status: FiveElementStatus,
)
