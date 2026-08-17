package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteDeviceTokenDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.notification.DeviceTokenRequest
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class RemoteDeviceTokenDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteDeviceTokenDataSource {
        override suspend fun postDeviceToken(token: String) {
            client
                .get()
                .post(DEVICE_TOKENS_URL) {
                    setBody(DeviceTokenRequest(token = token, platform = "ANDROID"))
                }.body<CommonResponse<Unit>>()
        }

        companion object {
            private const val DEVICE_TOKENS_URL = "api/v1/notifications/device-tokens"
        }
    }
