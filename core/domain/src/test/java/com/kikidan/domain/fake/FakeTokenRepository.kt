package com.kikidan.domain.fake

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTokenRepository : TokenRepository {
    var loginStateResult: Result<Boolean> = Result.success(false)
    var getTokenResult: Result<AuthToken?> = Result.success(null)
    var saveTokenResult: Result<Unit> = Result.success(Unit)
    var clearTokenResult: Result<Unit> = Result.success(Unit)
    var clearTokenCalled = false

    override fun observeLoginState(): Flow<Result<Boolean>> = flowOf(loginStateResult)

    override suspend fun getToken(): Result<AuthToken?> = getTokenResult

    override suspend fun saveToken(token: AuthToken): Result<Unit> = saveTokenResult

    override suspend fun clearToken(): Result<Unit> {
        clearTokenCalled = true
        return clearTokenResult
    }
}
