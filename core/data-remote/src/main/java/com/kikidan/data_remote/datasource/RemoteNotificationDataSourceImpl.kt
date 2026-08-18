package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteNotificationDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.notification.NotificationListResponse
import com.kikidan.data_remote.dto.notification.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.notification.NotificationSummary
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import javax.inject.Inject

class RemoteNotificationDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteNotificationDataSource {
        override suspend fun getNotifications(): NotificationSummary =
            client
                .get()
                .get(NOTIFICATIONS_URL)
                .bodyNotNull<NotificationListResponse>()
                .toDomain()

        override suspend fun markAsRead(notificationId: String) {
            client
                .get()
                .patch(readUrl(notificationId))
                .body<CommonResponse<Unit>>()
        }

        companion object {
            private const val NOTIFICATIONS_URL = "api/v1/notifications"

            private fun readUrl(notificationId: String) = "$NOTIFICATIONS_URL/$notificationId/read"
        }
    }
