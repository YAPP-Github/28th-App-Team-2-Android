package com.kikidan.data_remote.fake

import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLocalTokenDataSource : LocalTokenDataSource {
    private val tokenFlow = MutableStateFlow<AuthToken?>(null)

    var clearTokenCalled = false
    var savedToken: AuthToken? = null

    override fun observeToken(): Flow<AuthToken?> = tokenFlow

    override suspend fun getToken(): AuthToken? = tokenFlow.value

    override suspend fun saveToken(token: AuthToken) {
        savedToken = token
        tokenFlow.value = token
    }

    override suspend fun clearToken() {
        clearTokenCalled = true
        savedToken = null
        tokenFlow.value = null
    }
}
