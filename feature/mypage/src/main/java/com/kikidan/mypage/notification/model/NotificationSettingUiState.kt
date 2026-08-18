package com.kikidan.mypage.notification.model

sealed interface NotificationSettingUiState {
    data object Loading : NotificationSettingUiState

    data class Success(
        val model: NotificationSettingUiModel,
    ) : NotificationSettingUiState

    data class Fail(
        val e: Throwable,
    ) : NotificationSettingUiState
}
