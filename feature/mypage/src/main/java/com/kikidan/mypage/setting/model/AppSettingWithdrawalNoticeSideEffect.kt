package com.kikidan.mypage.setting.model

sealed interface AppSettingWithdrawalNoticeSideEffect {
    data object WithdrawalSucceeded : AppSettingWithdrawalNoticeSideEffect
}
