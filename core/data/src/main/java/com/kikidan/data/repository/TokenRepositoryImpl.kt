package com.kikidan.data.repository

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.TokenDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenRepositoryImpl
    @Inject
    constructor(
        private val tokenDataSource: TokenDataSource,
        private val invalidator: AuthTokenCacheInvalidator,
    ) : TokenRepository {
        override fun observeLoginState(): Flow<Result<Boolean>> =
            tokenDataSource
                .observeToken()
                .map { token -> Result.success(token != null) }
                // DataStore.data 는 파일 손상 시 IOException 을 방출한다.
                // catch 가 없으면 예외가 구독자로 누수돼 크래시한다(rules/20-data).
                .catch { error -> emit(Result.failure(error)) }

        override suspend fun getToken(): Result<AuthToken?> = runCatching { tokenDataSource.getToken() }

        override suspend fun saveToken(token: AuthToken): Result<Unit> =
            runCatching {
                tokenDataSource.saveToken(token)
                invalidator.invalidate()
            }

        override suspend fun clearToken(): Result<Unit> =
            runCatching {
                tokenDataSource.clearToken()
                invalidator.invalidate()
            }
    }
