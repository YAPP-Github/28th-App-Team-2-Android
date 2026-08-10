package com.kikidan.domain.usecase.user

import com.kikidan.domain.di.Fake
import com.kikidan.domain.model.user.User
import com.kikidan.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase
    @Inject
    constructor(
        @Fake private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(user: User): Result<Unit> = userRepository.updateUserInfo(user)
    }
