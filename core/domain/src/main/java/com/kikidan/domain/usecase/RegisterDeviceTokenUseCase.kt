package com.kikidan.domain.usecase

import com.kikidan.domain.repository.DeviceTokenRepository
import javax.inject.Inject

class RegisterDeviceTokenUseCase
    @Inject
    constructor(
        private val deviceTokenRepository: DeviceTokenRepository,
    ) {
        suspend operator fun invoke(token: String): Result<Unit> = deviceTokenRepository.registerDeviceToken(token)
    }
