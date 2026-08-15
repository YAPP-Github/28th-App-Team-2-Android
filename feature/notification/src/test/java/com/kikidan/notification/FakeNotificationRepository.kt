package com.kikidan.notification

import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.repository.NotificationRepository

class FakeNotificationRepository : NotificationRepository {
    var notificationsResult: Result<NotificationSummary> = Result.failure(NotImplementedError())
    var markAsReadResult: Result<Unit> = Result.success(Unit)
    var lastMarkedAsReadId: String? = null

    override suspend fun getNotifications(): Result<NotificationSummary> = notificationsResult

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        lastMarkedAsReadId = notificationId
        return markAsReadResult
    }
}
