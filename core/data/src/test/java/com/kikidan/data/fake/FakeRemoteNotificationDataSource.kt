package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteNotificationDataSource
import com.kikidan.domain.model.notification.NotificationSummary

class FakeRemoteNotificationDataSource : RemoteNotificationDataSource {
    var notifications: NotificationSummary? = null
    var throwOnGetNotifications: Throwable? = null

    var throwOnMarkAsRead: Throwable? = null
    var lastMarkedAsReadId: String? = null

    override suspend fun getNotifications(): NotificationSummary {
        throwOnGetNotifications?.let { throw it }
        return notifications!!
    }

    override suspend fun markAsRead(notificationId: String) {
        lastMarkedAsReadId = notificationId
        throwOnMarkAsRead?.let { throw it }
    }
}
