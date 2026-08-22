package com.kikidan.domain.usecase

import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import javax.inject.Inject

class LogoutUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val tokenRepository: TokenRepository,
    ) {
        // 서버 로그아웃 요청이 네트워크 등으로 실패해도 로컬 토큰은 항상 제거해 기기에서는 로그아웃 상태로 만든다.
        suspend operator fun invoke(): Result<Unit> {
            authRepository.logout()
            return tokenRepository.clearToken()
        }
    }
