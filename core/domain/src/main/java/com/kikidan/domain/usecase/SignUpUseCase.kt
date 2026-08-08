package com.kikidan.domain.usecase

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import javax.inject.Inject

class SignUpUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val tokenRepository: TokenRepository,
    ) {
        suspend operator fun invoke(
            signupSubmission: SignupSubmission,
            onboardingToken: OnboardingToken,
        ): Result<AuthToken> {
            val authToken =
                authRepository.signup(signupSubmission, onboardingToken).getOrElse { return Result.failure(it) }
            tokenRepository.saveToken(authToken).getOrElse { return Result.failure(it) }
            return Result.success(authToken)
        }
    }
