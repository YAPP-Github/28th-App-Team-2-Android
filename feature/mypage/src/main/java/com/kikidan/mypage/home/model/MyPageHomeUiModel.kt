package com.kikidan.mypage.home.model

import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.user.User

data class MyPageHomeUiModel(
    val user: User,
    val sajuPalja: SajuPalja,
    val appVersionName: String,
    val isLatestVersion: Boolean = true,
)
