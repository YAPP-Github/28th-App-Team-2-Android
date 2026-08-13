package com.kikidan.data.repository

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.repository.NotificationRepository
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

        override suspend fun getNotificationSetting(): Result<NotificationSetting> = Result.success(setting)

        override suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit> {
            this.setting = setting
            return Result.success(Unit)
        }
    }
