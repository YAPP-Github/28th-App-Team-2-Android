package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteNotificationDataSource
import com.kikidan.data_remote.dto.notification.NotificationSettingResponse
import com.kikidan.data_remote.dto.notification.toDomain
import com.kikidan.data_remote.dto.notification.toUpdateRequest
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.notification.NotificationSetting
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import javax.inject.Inject

class RemoteNotificationDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteNotificationDataSource {
        override suspend fun getNotificationSetting(): NotificationSetting =
            client.get().get(SETTINGS_URL).bodyNotNull<NotificationSettingResponse>().toDomain()

        override suspend fun updateNotificationSetting(setting: NotificationSetting) {
            client.get().patch(SETTINGS_URL) { setBody(setting.toUpdateRequest()) }
                .bodyNotNull<NotificationSettingResponse>()
        }

        companion object {
            private const val SETTINGS_URL = "api/v1/notifications/settings"
        }
    }
