package com.kikidan.mypage.mansaeryeok.model

import androidx.annotation.StringRes
import com.kikidan.domain.model.saju.SajuPillar

data class SajuPillarDetail(
    val pillar: SajuPillar,
    @param:StringRes val pillarLabelRes: Int,
    @param:StringRes val periodLabelRes: Int,
    val topTenGod: TenGod,
    val bottomTenGod: TenGod,
    val hiddenStem: String,
    val twelveUnseong: TwelveUnseong,
    val twelveSinsal: TwelveSinsal,
)
