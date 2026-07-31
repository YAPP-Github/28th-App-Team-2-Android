package com.kikidan.data.repository

import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val remoteAuthDataSource: RemoteAuthDataSource,
    ) : AuthRepository {
        override suspend fun login(credential: OAuthCredential): Result<LoginResult> =
            runCatchingCancellable {
                remoteAuthDataSource.login(credential)
            }
    }
