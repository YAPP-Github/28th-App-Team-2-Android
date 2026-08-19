package com.kikidan.domain.repository

import com.kikidan.domain.model.user.User
import com.kikidan.domain.model.user.WithdrawalReason

interface UserRepository {
    suspend fun getUserInfo(): Result<User>

    suspend fun updateUserInfo(user: User): Result<Unit>

    suspend fun withdraw(
        reason: WithdrawalReason,
        detail: String?,
    ): Result<Unit>
}
