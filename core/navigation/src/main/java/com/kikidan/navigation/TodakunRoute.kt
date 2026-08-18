package com.kikidan.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TodakunRoute : NavKey {
    @Serializable
    data object Login : TodakunRoute

    @Serializable
    data class Onboarding(
        val onboardingToken: String,
    ) : TodakunRoute

    @Serializable
    data class Terms(
        val onboardingToken: String,
    ) : TodakunRoute

    @Serializable
    data object Home : TodakunRoute

    @Serializable
    data class Chat(
        val conversationId: String? = null,
    ) : TodakunRoute

    @Serializable
    data object ChatHistory : TodakunRoute

    @Serializable
    data object LuckAction : TodakunRoute

    // TODO(#후속이슈): feature:mypage 구현 후 실제 화면 연결
    @Serializable
    data object MyPage : TodakunRoute

    @Serializable
    data class FortuneReport(
        val fortuneId: String,
    ) : TodakunRoute

    @Serializable
    data object Notification : TodakunRoute

    // TODO(#후속이슈): 상대방 사주 라우트 — feature:saju-contents 구현 후 추가
}
