package com.kikidan.notification.model

import android.text.format.DateUtils
import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.model.notification.NotificationType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class NotificationUiModel(
    val id: String,
    val category: NotificationType,
    val title: String,
    val relativeTime: String,
    val isRead: Boolean,
)

fun Notification.toUiModel(): NotificationUiModel =
    NotificationUiModel(
        id = id,
        category = type,
        title = title,
        relativeTime =
            DateUtils
                .getRelativeTimeSpanString(
                    createdAt.toEpochMilli(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                )?.toString() ?: DateTimeFormatter
                .ofPattern("yyyy.MM.dd")
                .withZone(ZoneId.systemDefault())
                .format(createdAt),
        isRead = isRead,
    )
