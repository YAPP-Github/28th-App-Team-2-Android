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

    @Serializable
    data object MyPage : TodakunRoute

    @Serializable
    data object MyPageEdit : TodakunRoute

    @Serializable
    data object Mansaeryeok : TodakunRoute

    @Serializable
    data object NotificationSetting : TodakunRoute

    @Serializable
    data object PartnerSajuManagement : TodakunRoute

    @Serializable
    data class PartnerSajuForm(
        val partnerLinkId: String? = null,
    ) : TodakunRoute

    @Serializable
    data object AppSetting : TodakunRoute

    @Serializable
    data object AppSettingWithdrawal : TodakunRoute

    @Serializable
    data class AppSettingWithdrawalNotice(
        val reason: String,
        val detailReason: String,
    ) : TodakunRoute

    @Serializable
    data object CompatibilityInput : TodakunRoute

    @Serializable
    data class CompatibilityResult(
        val compatibilityId: String,
        val partnerLinkId: String? = null,
    ) : TodakunRoute

    @Serializable
    data object DateFortuneInput : TodakunRoute

    @Serializable
    data class DateFortuneResult(
        val ids: List<String>,
    ) : TodakunRoute

    @Serializable
    data object YearFortuneInput : TodakunRoute

    @Serializable
    data class YearFortuneResult(
        val id: String,
    ) : TodakunRoute

    @Serializable
    data class FortuneReport(
        val fortuneId: String,
    ) : TodakunRoute

    @Serializable
    data object Notification : TodakunRoute
}
