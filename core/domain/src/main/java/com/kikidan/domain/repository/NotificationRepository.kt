package com.kikidan.domain.repository

import com.kikidan.domain.model.notification.NotificationSummary

interface NotificationRepository {
    suspend fun getNotifications(): Result<NotificationSummary>

    suspend fun markAsRead(notificationId: String): Result<Unit>
}
