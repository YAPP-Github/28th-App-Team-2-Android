package com.kikidan.data.datasource

import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow

// rules/20-data: DataSource는 try-catch 하지 않고 에러를 그대로 throw한다.
interface TokenDataSource {
    fun observeToken(): Flow<AuthToken?>

    suspend fun getToken(): AuthToken?

    suspend fun saveToken(token: AuthToken)

    suspend fun clearToken()
}
