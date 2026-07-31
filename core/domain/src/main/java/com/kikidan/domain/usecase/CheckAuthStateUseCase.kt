package com.kikidan.domain.usecase

import com.kikidan.domain.model.auth.AuthState
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import javax.inject.Inject

class CheckAuthStateUseCase
    @Inject
    constructor(
        private val tokenRepository: TokenRepository,
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(): AuthState {
            val token = tokenRepository.getToken().getOrNull() ?: return AuthState.Unauthenticated
            return authRepository
                .refresh(token.refreshToken)
                .fold(
                    onSuccess = { newToken ->
                        tokenRepository.saveToken(newToken)
                        AuthState.Authenticated
                    },
                    onFailure = {
                        tokenRepository.clearToken()
                        AuthState.Unauthenticated
                    },
                )
        }
    }
