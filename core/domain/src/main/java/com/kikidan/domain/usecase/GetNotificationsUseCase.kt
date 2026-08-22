package com.kikidan.domain.usecase

import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) {
        suspend operator fun invoke(): Result<NotificationSummary> = notificationRepository.getNotifications()
    }
