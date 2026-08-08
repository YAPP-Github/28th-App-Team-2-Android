package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val remoteAuthDataSource: RemoteAuthDataSource,
    ) : AuthRepository {
        override suspend fun login(credential: OAuthCredential): Result<LoginResult> =
            runCatchingCancellable {
                remoteAuthDataSource.postLogin(credential)
            }

        override suspend fun refresh(refreshToken: String): Result<AuthToken> =
            runCatchingCancellable {
                remoteAuthDataSource.postRefresh(refreshToken)
            }

        override suspend fun signup(
            signupSubmission: SignupSubmission,
            onboardingToken: OnboardingToken,
        ): Result<AuthToken> =
            runCatchingCancellable {
                remoteAuthDataSource.postSignup(signupSubmission, onboardingToken)
            }
    }
