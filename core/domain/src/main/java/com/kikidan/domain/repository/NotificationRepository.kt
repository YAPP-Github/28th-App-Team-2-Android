package com.kikidan.domain.repository

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.model.notification.NotificationSummary

interface NotificationRepository {
    suspend fun getNotificationSetting(): Result<NotificationSetting>

    suspend fun getNotifications(): Result<NotificationSummary>

    suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit>

    suspend fun markAsRead(notificationId: String): Result<Unit>
}
