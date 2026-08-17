package com.kikidan.domain.repository

import com.kikidan.domain.model.notification.NotificationSetting

interface NotificationRepository {
    suspend fun getNotificationSetting(): Result<NotificationSetting>

    suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit>
}
