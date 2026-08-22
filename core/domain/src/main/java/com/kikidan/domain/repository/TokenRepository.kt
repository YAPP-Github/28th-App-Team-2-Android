package com.kikidan.domain.repository

import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    fun observeLoginState(): Flow<Result<Boolean>>

    suspend fun getToken(): Result<AuthToken?>

    suspend fun saveToken(token: AuthToken): Result<Unit>

    suspend fun clearToken(): Result<Unit>
}
