package com.kikidan.onboarding.fake

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTokenRepository : TokenRepository {
    var saveResult: Result<Unit> = Result.success(Unit)

    override fun observeLoginState(): Flow<Result<Boolean>> = flowOf()

    override suspend fun getToken(): Result<AuthToken?> = error("not used")

    override suspend fun saveToken(token: AuthToken): Result<Unit> = saveResult

    override suspend fun clearToken(): Result<Unit> = error("not used")
}
