package com.kikidan.domain.fake

import com.kikidan.domain.repository.DeviceTokenRepository

class FakeDeviceTokenRepository : DeviceTokenRepository {
    var lastRegisteredToken: String? = null
    var registerResult: Result<Unit> = Result.success(Unit)

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        lastRegisteredToken = token
        return registerResult
    }
}
