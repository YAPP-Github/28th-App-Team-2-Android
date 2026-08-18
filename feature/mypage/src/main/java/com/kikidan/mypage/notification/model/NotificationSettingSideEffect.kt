package com.kikidan.mypage.notification.model

sealed interface NotificationSettingSideEffect {
    data object NavigateBack : NotificationSettingSideEffect
}
