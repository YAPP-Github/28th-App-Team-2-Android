package com.kikidan.domain.usecase.user

import com.kikidan.domain.model.user.User
import com.kikidan.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<User> = userRepository.getUserInfo()
}