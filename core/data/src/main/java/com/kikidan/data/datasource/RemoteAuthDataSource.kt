package com.kikidan.data.datasource

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential

interface RemoteAuthDataSource {
    suspend fun postLogin(oauthCredential: OAuthCredential): LoginResult

    suspend fun postRefresh(refreshToken: String): AuthToken
}
