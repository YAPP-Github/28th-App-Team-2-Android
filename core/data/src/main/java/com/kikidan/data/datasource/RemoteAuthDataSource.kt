package com.kikidan.data.datasource

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OAuthToken
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.user.User

interface RemoteAuthDataSource {
    suspend fun postLogin(oauthCredential: OAuthCredential): LoginResult

    suspend fun postSignup(
        user: User,
        onboardingToken: OnboardingToken,
    ): AuthToken

    suspend fun postLogout()

    suspend fun postRefresh(refreshToken: String): AuthToken
}
