package com.kikidan.data.datasource

import com.kikidan.domain.model.notification.NotificationSummary

interface RemoteNotificationDataSource {
    suspend fun getNotifications(): NotificationSummary

    suspend fun markAsRead(notificationId: String)
}
