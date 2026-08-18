package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteUserDataSource
import com.kikidan.domain.model.user.User
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
    }
