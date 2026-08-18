package com.kikidan.data.datasource

import com.kikidan.domain.model.notification.NotificationSetting

interface RemoteNotificationDataSource {
    suspend fun getNotificationSetting(): NotificationSetting

    suspend fun updateNotificationSetting(setting: NotificationSetting)
}
