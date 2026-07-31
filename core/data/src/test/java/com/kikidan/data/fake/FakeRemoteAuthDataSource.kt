package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential

class FakeRemoteAuthDataSource : RemoteAuthDataSource {
    var loginResult: LoginResult = LoginResult(AuthToken("access", "refresh"), "onboarding", newMember = false)
    var throwOnLogin: Throwable? = null

    override suspend fun postLogin(oauthCredential: OAuthCredential): LoginResult {
        throwOnLogin?.let { throw it }
        return loginResult
    }

    override suspend fun postRefresh(refreshToken: String): AuthToken = error("not used")
}
