package com.kikidan.data.datasource

import com.kikidan.domain.model.auth.AuthToken

interface RemoteAuthDataSource {
    suspend fun postRefresh(refreshToken: String): AuthToken
}
