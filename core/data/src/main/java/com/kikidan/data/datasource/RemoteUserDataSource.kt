package com.kikidan.data.datasource

import com.kikidan.domain.model.user.User
import com.kikidan.domain.model.user.WithdrawalReason

interface RemoteUserDataSource {
    suspend fun getUser(): User

    suspend fun updateUser(user: User)

    suspend fun withdraw(
        reason: WithdrawalReason,
        detail: String?,
    )
}
