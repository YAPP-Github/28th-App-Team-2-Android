package com.kikidan.data.datasource

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.model.notification.NotificationSummary

interface RemoteNotificationDataSource {
    suspend fun getNotificationSetting(): NotificationSetting

    suspend fun getNotifications(): NotificationSummary

    suspend fun updateNotificationSetting(setting: NotificationSetting)

    suspend fun markAsRead(notificationId: String)
}
