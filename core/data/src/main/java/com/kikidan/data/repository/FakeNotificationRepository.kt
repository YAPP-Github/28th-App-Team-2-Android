package com.kikidan.data.repository

import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.model.notification.NotificationType
import com.kikidan.domain.repository.NotificationRepository
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeNotificationRepository
    @Inject
    constructor() : NotificationRepository {
        private var setting =
            NotificationSetting(
                morningReportEnabled = true,
                morningReportTime = LocalTime.of(8, 0),
                todakiEnabled = true,
                luckyActionReminderEnabled = true,
            )

        private var notifications =
            listOf(
                Notification(
                    id = "1",
                    type = NotificationType.FORTUNE,
                    title = "오늘의 운세가 도착했어요",
                    content = "지금 확인해보세요",
                    deepLink = "todakun://fortune",
                    isRead = false,
                    createdAt = Instant.now(),
                ),
                Notification(
                    id = "2",
                    type = NotificationType.LUCKY_ACTION,
                    title = "럭키 액션 리마인더",
                    content = "오늘의 럭키 액션을 완료해보세요",
                    deepLink = "todakun://luck-action",
                    isRead = true,
                    createdAt = Instant.now(),
                ),
            )

        override suspend fun getNotificationSetting(): Result<NotificationSetting> = Result.success(setting)

        override suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit> {
            this.setting = setting
            return Result.success(Unit)
        }

        override suspend fun getNotifications(): Result<NotificationSummary> =
            Result.success(
                NotificationSummary(
                    unreadCount = notifications.count { !it.isRead },
                    notifications = notifications,
                ),
            )

        override suspend fun markAsRead(notificationId: String): Result<Unit> {
            notifications = notifications.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            return Result.success(Unit)
        }
    }
