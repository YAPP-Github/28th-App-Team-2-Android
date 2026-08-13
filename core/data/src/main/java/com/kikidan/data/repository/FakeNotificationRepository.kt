package com.kikidan.data.repository

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeNotificationRepository
    @Inject
    constructor() : NotificationRepository {
        private var setting =
            NotificationSetting(
                morningReportEnabled = true,
                morningReportHour = 8,
                morningReportMinute = 0,
                todakAlarmEnabled = true,
                luckyActionReminderEnabled = true,
            )

        override suspend fun getNotificationSetting(): Result<NotificationSetting> = Result.success(setting)

        override suspend fun updateNotificationSetting(setting: NotificationSetting): Result<Unit> {
            this.setting = setting
            return Result.success(Unit)
        }
    }
