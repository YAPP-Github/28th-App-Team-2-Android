package com.kikidan.domain.model.notification

import java.time.Instant

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val deepLink: String,
    val isRead: Boolean,
    val createdAt: Instant,
)
