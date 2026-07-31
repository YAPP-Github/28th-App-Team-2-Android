package com.kikidan.domain.usecase

import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val tokenRepository: TokenRepository,
    ) {
        suspend operator fun invoke(credential: OAuthCredential): Result<LoginResult> {
            val result = authRepository.login(credential).getOrElse { return Result.failure(it) }
            tokenRepository.saveToken(result.authToken).getOrElse { return Result.failure(it) }
            return Result.success(result)
        }
    }
