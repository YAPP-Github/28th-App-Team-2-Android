package com.kikidan.domain.fake

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var loginResult: Result<LoginResult> = Result.failure(IllegalStateException("not used"))
    var signupResult: Result<AuthToken> = Result.failure(IllegalStateException("not used"))
    var refreshResult: Result<AuthToken> = Result.failure(IllegalStateException("not used"))
    var logoutResult: Result<Unit> = Result.success(Unit)
    var logoutCalled = false

    override suspend fun login(credential: OAuthCredential): Result<LoginResult> = loginResult

    override suspend fun signup(
        signupSubmission: SignupSubmission,
        onboardingToken: OnboardingToken,
    ): Result<AuthToken> = signupResult

    override suspend fun refresh(refreshToken: String): Result<AuthToken> = refreshResult

    override suspend fun logout(): Result<Unit> {
        logoutCalled = true
        return logoutResult
    }
}
