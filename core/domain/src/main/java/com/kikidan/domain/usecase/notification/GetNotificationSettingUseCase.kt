package com.kikidan.domain.usecase.notification

import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationSettingUseCase
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) {
        suspend operator fun invoke(): Result<NotificationSetting> = notificationRepository.getNotificationSetting()
    }
