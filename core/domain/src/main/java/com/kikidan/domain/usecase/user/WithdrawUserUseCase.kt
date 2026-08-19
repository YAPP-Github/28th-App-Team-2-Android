package com.kikidan.domain.usecase.user

import com.kikidan.domain.model.user.WithdrawalReason
import com.kikidan.domain.repository.UserRepository
import javax.inject.Inject

class WithdrawUserUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(
            reason: WithdrawalReason,
            detail: String?,
        ): Result<Unit> = userRepository.withdraw(reason, detail)
    }
