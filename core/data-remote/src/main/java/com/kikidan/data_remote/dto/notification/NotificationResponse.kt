package com.kikidan.data_remote.dto.notification

import com.kikidan.data_remote.util.toInstantOrThrow
import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.model.notification.NotificationType
import kotlinx.serialization.Serializable

@Serializable
data class NotificationListResponse(
    val unreadCount: Int = 0,
    val notifications: List<NotificationResponse> = emptyList(),
)

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val content: String = "",
    val deepLink: String = "",
    val isRead: Boolean = false,
    val createdAt: String,
)

internal fun NotificationListResponse.toDomain(): NotificationSummary =
    NotificationSummary(
        unreadCount = unreadCount,
        notifications = notifications.map { it.toDomain() },
    )

internal fun NotificationResponse.toDomain(): Notification =
    Notification(
        id = id,
        type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.NOTICE),
        title = title,
        content = content,
        deepLink = deepLink,
        isRead = isRead,
        createdAt = createdAt.toInstantOrThrow(),
    )
