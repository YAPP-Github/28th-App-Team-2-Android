package com.kikidan.domain.usecase

import com.kikidan.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) {
        suspend operator fun invoke(notificationId: String): Result<Unit> =
            notificationRepository.markAsRead(notificationId)
    }
