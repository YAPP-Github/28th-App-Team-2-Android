package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteNotificationDataSource
import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.model.notification.NotificationSummary
import java.time.LocalTime

class FakeRemoteNotificationDataSource : RemoteNotificationDataSource {
    var notifications: NotificationSummary? = null
    var throwOnGetNotifications: Throwable? = null

    var throwOnMarkAsRead: Throwable? = null
    var lastMarkedAsReadId: String? = null

    var notificationSetting: NotificationSetting =
        NotificationSetting(
            morningReportEnabled = true,
            morningReportTime = LocalTime.of(8, 0),
            todakiEnabled = true,
            luckyActionReminderEnabled = true,
        )
    var throwOnGetNotificationSetting: Throwable? = null

    var throwOnUpdateNotificationSetting: Throwable? = null
    var lastUpdatedSetting: NotificationSetting? = null

    override suspend fun getNotifications(): NotificationSummary {
        throwOnGetNotifications?.let { throw it }
        return notifications!!
    }

    override suspend fun markAsRead(notificationId: String) {
        lastMarkedAsReadId = notificationId
        throwOnMarkAsRead?.let { throw it }
    }

    override suspend fun getNotificationSetting(): NotificationSetting {
        throwOnGetNotificationSetting?.let { throw it }
        return notificationSetting
    }

    override suspend fun updateNotificationSetting(setting: NotificationSetting) {
        lastUpdatedSetting = setting
        throwOnUpdateNotificationSetting?.let { throw it }
    }
}
