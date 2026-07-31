package com.kikidan.domain.usecase

import com.kikidan.domain.model.auth.AuthState
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import javax.inject.Inject

/**
 * 로컬 토큰 존재 여부와 서버 세션 유효성을 함께 확인한다.
 *
 * 토큰이 없으면 서버를 호출하지 않고 즉시 [AuthState.Unauthenticated]를 반환한다.
 * 토큰이 있으면 리프레시 토큰으로 `POST /auth/refresh`를 호출해 세션이 살아있는지 검증한다.
 * 성공하면 새로 발급된 토큰을 저장하고, 실패하면(리프레시 토큰까지 무효) 로컬 토큰을 지우고
 * 로그아웃 상태로 본다. 이 호출은 Ktor Bearer Auth 플러그인의 자동 리프레시(401 발생 시에만
 * 동작)와 별개의 명시적 호출이라, 실패했을 때 토큰 정리도 직접 해줘야 한다.
 */
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
