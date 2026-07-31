package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.user.User

class FakeRemoteAuthDataSource : RemoteAuthDataSource {
    var loginResult: LoginResult =
        LoginResult(AuthToken("access", "refresh"), OnboardingToken("onboarding"), newMember = false)
    var throwOnLogin: Throwable? = null

    override suspend fun postLogin(oauthCredential: OAuthCredential): LoginResult {
        throwOnLogin?.let { throw it }
        return loginResult
    }

    override suspend fun postSignup(
        user: User,
        onboardingToken: OnboardingToken,
    ): AuthToken = error("not used")

    override suspend fun postLogout() = error("not used")

    override suspend fun postRefresh(refreshToken: String): AuthToken = error("not used")
}
