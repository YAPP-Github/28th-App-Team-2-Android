package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteDeviceTokenDataSource
import com.kikidan.domain.repository.DeviceTokenRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class DeviceTokenRepositoryImpl
    @Inject
    constructor(
        private val remoteDeviceTokenDataSource: RemoteDeviceTokenDataSource,
    ) : DeviceTokenRepository {
        override suspend fun registerDeviceToken(token: String): Result<Unit> =
            runCatchingCancellable { remoteDeviceTokenDataSource.postDeviceToken(token) }
    }
