package com.kikidan.notification.model

sealed interface NotificationSideEffect {
    data class Error(
        val e: Throwable,
    ) : NotificationSideEffect

    data class NavigateToDeepLink(
        val deepLink: String,
    ) : NotificationSideEffect
}
