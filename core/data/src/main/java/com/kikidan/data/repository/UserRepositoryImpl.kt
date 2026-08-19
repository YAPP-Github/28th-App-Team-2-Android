package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteUserDataSource
import com.kikidan.domain.model.user.User
import com.kikidan.domain.model.user.WithdrawalReason
import com.kikidan.domain.repository.UserRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        private val remoteUserDataSource: RemoteUserDataSource,
    ) : UserRepository {
        override suspend fun getUserInfo(): Result<User> = runCatchingCancellable { remoteUserDataSource.getUser() }

        override suspend fun updateUserInfo(user: User): Result<Unit> =
            runCatchingCancellable { remoteUserDataSource.updateUser(user) }

        override suspend fun withdraw(
            reason: WithdrawalReason,
            detail: String?,
        ): Result<Unit> = runCatchingCancellable { remoteUserDataSource.withdraw(reason, detail) }
    }
