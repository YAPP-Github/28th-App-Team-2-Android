package com.kikidan.data.repository

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.repository.TokenRepository
import com.kikidan.domain.util.runCatchingCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenRepositoryImpl
    @Inject
    constructor(
        private val localTokenDataSource: LocalTokenDataSource,
        private val tokenCacheInvalidator: AuthTokenCacheInvalidator,
    ) : TokenRepository {
        override fun observeLoginState(): Flow<Result<Boolean>> =
            localTokenDataSource
                .observeToken()
                .map { token -> Result.success(token != null) }
                .catch { error -> emit(Result.failure(error)) }

        override suspend fun getToken(): Result<AuthToken?> = runCatchingCancellable { localTokenDataSource.getToken() }

        override suspend fun saveToken(token: AuthToken): Result<Unit> =
            runCatchingCancellable {
                localTokenDataSource.saveToken(token)
                tokenCacheInvalidator.invalidate()
            }

        override suspend fun clearToken(): Result<Unit> =
            runCatchingCancellable {
                localTokenDataSource.clearToken()
                tokenCacheInvalidator.invalidate()
            }
    }
