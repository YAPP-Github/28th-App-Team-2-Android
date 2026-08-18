package com.kikidan.domain.model.notification

data class PushNotificationEvent(
    val notificationId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val deepLink: String?,
) {
    companion object {
        const val DEEP_LINK_KEY = "deepLink"
    }
}
