package com.kikidan.data.fake

import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeLocalTokenDataSource : LocalTokenDataSource {
    private val tokenFlow = MutableStateFlow<AuthToken?>(null)
    var throwOnGet: Throwable? = null
    var throwOnObserve: Throwable? = null

    fun emit(token: AuthToken?) {
        tokenFlow.value = token
    }

    override fun observeToken(): Flow<AuthToken?> = throwOnObserve?.let { error -> flow { throw error } } ?: tokenFlow

    override suspend fun getToken(): AuthToken? {
        throwOnGet?.let { throw it }
        return tokenFlow.value
    }

    override suspend fun saveToken(token: AuthToken) {
        tokenFlow.value = token
    }

    override suspend fun clearToken() {
        tokenFlow.value = null
    }
}
