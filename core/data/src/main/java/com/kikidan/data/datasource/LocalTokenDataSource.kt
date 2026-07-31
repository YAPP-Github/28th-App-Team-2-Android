package com.kikidan.data.datasource

import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow

interface LocalTokenDataSource {
    fun observeToken(): Flow<AuthToken?>

    suspend fun getToken(): AuthToken?

    suspend fun saveToken(token: AuthToken)

    suspend fun clearToken()
}
