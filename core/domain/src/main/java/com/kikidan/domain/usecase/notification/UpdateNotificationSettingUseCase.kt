package com.kikidan.domain.usecase.notification

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.repository.NotificationRepository
import javax.inject.Inject

class UpdateNotificationSettingUseCase
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) {
        suspend operator fun invoke(setting: NotificationSetting): Result<Unit> =
            notificationRepository.updateNotificationSetting(setting)
    }
