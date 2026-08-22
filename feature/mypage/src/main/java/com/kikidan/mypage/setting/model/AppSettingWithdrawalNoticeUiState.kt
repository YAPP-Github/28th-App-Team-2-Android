package com.kikidan.mypage.setting.model

sealed interface AppSettingWithdrawalNoticeUiState {
    data object Idle : AppSettingWithdrawalNoticeUiState

    data object Loading : AppSettingWithdrawalNoticeUiState

    data object Failure : AppSettingWithdrawalNoticeUiState
}
