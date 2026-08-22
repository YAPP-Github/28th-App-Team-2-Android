package com.kikidan.domain.repository

interface DeviceTokenRepository {
    suspend fun registerDeviceToken(token: String): Result<Unit>
}
