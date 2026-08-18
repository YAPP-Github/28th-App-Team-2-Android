package com.kikidan.mypage.mansaeryeok.model

import com.kikidan.domain.model.user.User

data class MansaeryeokDetailUiModel(
    val user: User,
    val pillars: List<SajuPillarDetail>,
    val fiveElements: List<FiveElementDistribution>,
)
