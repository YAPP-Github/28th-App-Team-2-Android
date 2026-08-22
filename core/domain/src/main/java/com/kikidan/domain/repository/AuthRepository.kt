package com.kikidan.domain.repository

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.SignupSubmission

interface AuthRepository {
    suspend fun login(credential: OAuthCredential): Result<LoginResult>

    suspend fun signup(
        signupSubmission: SignupSubmission,
        onboardingToken: OnboardingToken,
    ): Result<AuthToken>

    suspend fun refresh(refreshToken: String): Result<AuthToken>

    suspend fun logout(): Result<Unit>
}
