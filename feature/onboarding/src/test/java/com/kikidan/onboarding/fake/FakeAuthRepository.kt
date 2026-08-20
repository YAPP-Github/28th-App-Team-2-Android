package com.kikidan.onboarding.fake

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var signupResult: Result<AuthToken> = Result.success(AuthToken("access", "refresh"))
    var signupCallCount = 0

    override suspend fun login(credential: OAuthCredential): Result<LoginResult> = error("not used")

    override suspend fun signup(
        signupSubmission: SignupSubmission,
        onboardingToken: OnboardingToken,
    ): Result<AuthToken> {
        signupCallCount++
        return signupResult
    }

    override suspend fun refresh(refreshToken: String): Result<AuthToken> = error("not used")

    override suspend fun logout(): Result<Unit> = error("not used")
}
