package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteNotificationDataSource
import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.repository.NotificationRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class NotificationRepositoryImpl
    @Inject
    constructor(
        private val remoteNotificationDataSource: RemoteNotificationDataSource,
    ) : NotificationRepository {
        override suspend fun getNotificationSetting(): Result<NotificationSetting> =
            runCatchingCancellable { remoteNotificationDataSource.getNotificationSetting() }
        override suspend fun getNotifications(): Result<NotificationSummary> =
            runCatchingCancellable { remoteNotificationDataSource.getNotifications() }

        override suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit> =
            runCatchingCancellable { remoteNotificationDataSource.updateNotificationSetting(setting) }
        override suspend fun markAsRead(notificationId: String): Result<Unit> =
            runCatchingCancellable { remoteNotificationDataSource.markAsRead(notificationId) }
    }
