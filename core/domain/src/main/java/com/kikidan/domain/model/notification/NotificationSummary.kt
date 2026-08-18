package com.kikidan.domain.model.notification

data class NotificationSummary(
    val unreadCount: Int,
    val notifications: List<Notification>,
)
