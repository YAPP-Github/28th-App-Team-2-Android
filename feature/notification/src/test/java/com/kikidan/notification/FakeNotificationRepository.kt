package com.kikidan.notification

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.repository.NotificationRepository
import java.time.LocalTime

class FakeNotificationRepository : NotificationRepository {
    var notificationsResult: Result<NotificationSummary> = Result.failure(NotImplementedError())
    var markAsReadResult: Result<Unit> = Result.success(Unit)
    var lastMarkedAsReadId: String? = null

    var notificationSettingResult: Result<NotificationSetting> =
        Result.success(
            NotificationSetting(
                morningReportEnabled = true,
                morningReportTime = LocalTime.of(8, 0),
                todakiEnabled = true,
                luckyActionReminderEnabled = true,
            ),
        )
    var updateNotificationSettingResult: Result<Unit> = Result.success(Unit)
    var lastUpdatedSetting: NotificationSetting? = null

    override suspend fun getNotifications(): Result<NotificationSummary> = notificationsResult

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        lastMarkedAsReadId = notificationId
        return markAsReadResult
    }

    override suspend fun getNotificationSetting(): Result<NotificationSetting> = notificationSettingResult

    override suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit> {
        lastUpdatedSetting = setting
        return updateNotificationSettingResult
    }
}
