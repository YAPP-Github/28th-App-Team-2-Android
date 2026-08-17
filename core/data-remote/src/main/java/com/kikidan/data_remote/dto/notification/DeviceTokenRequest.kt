package com.kikidan.data_remote.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequest(
    val token: String,
    val platform: String,
)
